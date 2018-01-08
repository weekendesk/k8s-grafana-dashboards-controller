package com.mbenabda.kubernetes.controllers.grafana.dashboards;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.mbenabda.grafana.client.GrafanaClient;
import com.mbenabda.grafana.client.exceptions.GrafanaException;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

class ConfigMapsWatcher implements Watcher<ConfigMap> {
    private static final Joiner KEY_PARTS_JOINER = Joiner.on("/");
    private final GrafanaClient grafana;
    private final ObjectMapper mapper;
    private static final Logger LOGGER = Logger.getLogger(ConfigMapsWatcher.class.getSimpleName());

    public ConfigMapsWatcher(GrafanaClient grafana) {
        this.grafana = grafana;
        this.mapper =
                new ObjectMapper()
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    @Override
    public void eventReceived(Action action, ConfigMap configMap) {
        try {
            switch (action) {
                case ADDED: {
                    JsonNode dashboard = asDashboard(configMap);
                    grafana.importDashboard(dashboard);
                    LOGGER.info(format("ConfigMap %s was created. Dashboard %s has been added to Grafana", key(configMap), title(dashboard)));
                }
                break;

                case MODIFIED: {
                    JsonNode dashboard = asDashboard(configMap);
                    String title = title(dashboard);
                    String slug = grafana.slug(title);
                    grafana.deleteDashboard(slug);
                    grafana.importDashboard(dashboard);
                    LOGGER.info(format("ConfigMap %s was modified. Dashboard %s updated accordingly", key(configMap), title));
                }
                break;

                case DELETED: {
                    JsonNode dashboard = asDashboard(configMap);
                    String title = title(dashboard);
                    String slug = grafana.slug(title);
                    grafana.deleteDashboard(slug);
                    LOGGER.info(format("ConfigMap %s was deleted. Dashboard %s removed from Grafana", key(configMap), title));
                }
                break;
            }
        } catch (GrafanaException | IOException e) {
            LOGGER.warning(
                    format("Unable to handle %s event on ConfigMap %s because %s", action, key(configMap), e.getMessage())
            );
        }
    }

    private String key(ConfigMap configMap) {
        ObjectMeta metadata = configMap.getMetadata();

        return KEY_PARTS_JOINER.join(
                metadata.getNamespace(),
                metadata.getName()
        );
    }

    @Override
    public void onClose(KubernetesClientException e) {
        if (e != null) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private JsonNode asDashboard(ConfigMap configMap) throws IOException {
        return mapper.readTree(dashboardJson(configMap));
    }

    private String dashboardJson(ConfigMap configMap) {
        return valueOfFirstKey(configMap.getData());
    }

    private static <K, V> V valueOfFirstKey(Map<K, V> data) {
        return data.entrySet().iterator().next().getValue();
    }

    private static String title(JsonNode dashboard) {
        return dashboard.get("dashboard").get("title").asText();
    }
}
