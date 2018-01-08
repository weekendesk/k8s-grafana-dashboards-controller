package com.mbenabda.grafana.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import okhttp3.OkHttpClient;

import java.util.Base64;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class BasicAuthConfiguration extends GrafanaAuthConfiguration {
    private final String username;
    private final String password;

    public BasicAuthConfiguration(String username, String password) {
        this.username = requireNonNull(username);
        this.password = requireNonNull(password);
    }

    @Override
    OkHttpClient.Builder authorize(OkHttpClient.Builder builder) {
        return builder.addInterceptor(chain -> chain.proceed(
                chain.request()
                        .newBuilder()
                        .addHeader("Authorization", format("Basic %s", basicAuth(username, password)))
                        .build()
                )
        );
    }

    private String basicAuth(String username, String password) {
        return Base64.getEncoder().encodeToString(
                format("%s:%s", username, password).getBytes()
        );
    }
}
