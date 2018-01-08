package com.mbenabda.kubernetes.controllers.grafana.dashboards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbenabda.grafana.client.GrafanaClient;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import org.junit.Before;
import org.junit.Test;

import static io.fabric8.kubernetes.client.Watcher.Action.*;
import static java.lang.String.format;
import static org.mockito.Mockito.*;

public class ConfigMapsWatcherTest {
    private static final String SLUG = "first-dashboards-slug";
    private static final String TITLE = "dashboard 1";
    private GrafanaClient grafana;
    private ConfigMap CONFIGMAP;
    private JsonNode DASHBOARD;
    private ConfigMapsWatcher subject;

    @Before
    public void setUp() throws Exception {
        DASHBOARD = new ObjectMapper().readTree(format("{ \"dashboard\": { \"title\": \"%s\" } }", TITLE));
        CONFIGMAP = new ConfigMapBuilder()
                .withNewMetadata()
                .withNamespace("monitoring")
                .withName("first-dashboard")
                .endMetadata()
                .addToData("json", DASHBOARD.toString())
                .build();

        grafana = mock(GrafanaClient.class);
        subject = new ConfigMapsWatcher(grafana);
    }

    @Test
    public void should_import_dashboard_when_configmap_added() throws Exception {
        subject.eventReceived(ADDED, CONFIGMAP);

        verify(grafana).importDashboard(DASHBOARD);
    }

    @Test
    public void should_update_dashboard_when_watched_configmap_modified() throws Exception {
        when(grafana.slug(TITLE)).thenReturn(SLUG);

        subject.eventReceived(MODIFIED, CONFIGMAP);

        verify(grafana).deleteDashboard(SLUG);
        verify(grafana).importDashboard(DASHBOARD);
    }

    @Test
    public void should_delete_dashboard_when_watched_configmap_deleted() throws Exception {
        when(grafana.slug(TITLE)).thenReturn(SLUG);

        subject.eventReceived(DELETED, CONFIGMAP);

        verify(grafana).deleteDashboard(SLUG);
    }

}