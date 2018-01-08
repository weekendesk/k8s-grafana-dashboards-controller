package com.mbenabda.kubernetes.controllers.grafana.dashboards.configuration;

import com.beust.jcommander.IDefaultProvider;
import io.fabric8.kubernetes.client.Config;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
@NoArgsConstructor
public class K8sOptions {

    public static IDefaultProvider defaultsProvider() {
        return new NoDefaultsProvider();
    }

    public Config asClientConfig() {
        return Config.autoConfigure();
    }
}
