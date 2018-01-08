package com.mbenabda.kubernetes.controllers.grafana.dashboards.configuration;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Optional;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DashboardsWatchOptions {

    @Parameter(names = {"--watch-namespace"}, description = "Namespace to wath for Configmaps. defaults to the namespace the controller runs into.")
    private String namespace;

    @Parameter(names = {"--selector"}, description = "Configmaps selector. eg: label1=value1,label2=value2", converter = LabelSelectorConverter.class)
    private LabelSelector selector;


    public static IDefaultProvider defaultsProvider() {
        return new CompositeDefaultsProvider(
                ImmutableMap.of(
                        "--selector", DashboardsWatchOptions::defaultSelector
                )
        );
    }

    public FilterWatchListDeletable<ConfigMap, ConfigMapList, Boolean, Watch, Watcher<ConfigMap>> with(KubernetesClient k8s) {
        FilterWatchListDeletable<ConfigMap, ConfigMapList, Boolean, Watch, Watcher<ConfigMap>> filter = k8s
                .configMaps()
                .inNamespace(Optional.ofNullable(namespace).orElse(k8s.getNamespace()));

        if (selector != null) {
            filter = filter.withLabelSelector(selector);
        }

        return filter;
    }

    private static String defaultSelector() {
        return System.getenv("CONFIGMAP_SELECTOR");
    }
}
