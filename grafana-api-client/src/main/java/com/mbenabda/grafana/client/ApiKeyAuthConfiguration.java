package com.mbenabda.grafana.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import okhttp3.OkHttpClient;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class ApiKeyAuthConfiguration extends GrafanaAuthConfiguration {
    private final String apiKey;

    public ApiKeyAuthConfiguration(String apiKey) {
        this.apiKey = requireNonNull(apiKey);
    }

    @Override
    OkHttpClient.Builder authorize(OkHttpClient.Builder builder) {
        return builder.addInterceptor(chain -> chain.proceed(
                chain.request()
                        .newBuilder()
                        .addHeader("Authorization", format("Bearer %s", apiKey))
                        .build()
                )
        );
    }
}
