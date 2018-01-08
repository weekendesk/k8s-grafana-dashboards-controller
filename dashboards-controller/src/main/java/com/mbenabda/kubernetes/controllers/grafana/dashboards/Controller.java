package com.mbenabda.kubernetes.controllers.grafana.dashboards;

import com.mbenabda.grafana.client.GrafanaClient;
import com.mbenabda.kubernetes.controllers.grafana.dashboards.configuration.DashboardsWatchOptions;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.logging.Logger;

public class Controller {
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getSimpleName());

    private final DashboardsWatchOptions configMapsFiler;
    private final GrafanaClient grafana;
    private final KubernetesClient k8s;

    public Controller(KubernetesClient k8s, DashboardsWatchOptions configMapsFiler, GrafanaClient grafana) {
        this.k8s = k8s;
        this.configMapsFiler = configMapsFiler;
        this.grafana = grafana;
    }

    public void run() {
        configMapsFiler
                .with(k8s)
                .watch(
                        new ConfigMapsWatcher(
                                grafana
                        )
                );
    }
}
