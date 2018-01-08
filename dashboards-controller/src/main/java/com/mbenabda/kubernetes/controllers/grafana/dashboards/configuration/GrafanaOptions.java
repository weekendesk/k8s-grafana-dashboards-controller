package com.mbenabda.kubernetes.controllers.grafana.dashboards.configuration;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.ImmutableMap;
import com.mbenabda.grafana.client.ApiKeyAuthConfiguration;
import com.mbenabda.grafana.client.BasicAuthConfiguration;
import com.mbenabda.grafana.client.GrafanaAuthConfiguration;
import com.mbenabda.grafana.client.GrafanaConfiguration;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Accessors(fluent = true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class GrafanaOptions {

    @Getter
    @Parameter(names = {"--grafana-url", "-grafanaUrl"}, description = "URL of Grafana. Can also be specified through env variable GRAFANA_API_URL instead", required = true)
    private String host;

    @Parameter(names = {"--grafana-api-key"}, description = "Grafana API Key. Can also be specified through env variable GRAFANA_API_KEY instead", password = true)
    private String apiKey;

    @Parameter(names = {"--grafana-user", "-grafanaUsername"}, description = "Grafana User name (Basic Auth). Can also be specified through env variable GRAFANA_BASIC_AUTH_USERNAME or GRAFANA_USER instead")
    private String basicAuthUser;
    @Parameter(names = {"--grafana-password", "-grafanaPassword"}, description = "Grafana User password (Basic Auth). Can also be specified through env variable GRAFANA_BASIC_AUTH_PASSWORD or GRAFANA_PASSWORD instead", password = true)
    private String basicAuthPassword;

    public GrafanaConfiguration asClientConfig() {
        return new GrafanaConfiguration(
                host(),
                authConfig()
        );
    }

    public static IDefaultProvider defaultsProvider() {
        return new CompositeDefaultsProvider(
                ImmutableMap.of(
                        "--grafana-url", GrafanaOptions::defaultHost,
                        "--grafana-api-key", GrafanaOptions::defaultApiKey,
                        "--grafana-user", GrafanaOptions::defaultBasicAuthUsername,
                        "--grafana-password", GrafanaOptions::defaultBasicAuthPassword
                )
        );
    }

    private GrafanaAuthConfiguration authConfig() {
        return Stream.of(
                apiKeyAuth(),
                basicAuth()
        ).filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new ParameterException(
                        "You must provide either an API Key, or Basic auth credentials to authenticate against the Grafana API." +
                        "Run this command with the --help argument to see usage instructions."
                ));
    }

    private Optional<BasicAuthConfiguration> basicAuth() {
        return basicAuthUser == null || basicAuthPassword == null
                ? Optional.empty()
                : Optional.of(new BasicAuthConfiguration(
                basicAuthUser,
                basicAuthPassword
        ));
    }

    private Optional<ApiKeyAuthConfiguration> apiKeyAuth() {
        return Optional
                .ofNullable(apiKey)
                .map(ApiKeyAuthConfiguration::new);
    }

    private static String defaultHost() {
        return System.getenv("GRAFANA_API_URL");
    }

    private static String defaultApiKey() {
        return System.getenv("GRAFANA_API_KEY");
    }

    private static String defaultBasicAuthUsername() {
        return Stream.of(
                System.getenv("GRAFANA_BASIC_AUTH_USERNAME"),
                System.getenv("GRAFANA_USER")
        ).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static String defaultBasicAuthPassword() {
        return Stream.of(
                System.getenv("GRAFANA_BASIC_AUTH_PASSWORD"),
                System.getenv("GRAFANA_PASSWORD")
        ).filter(Objects::nonNull).findFirst().orElse(null);
    }
}
