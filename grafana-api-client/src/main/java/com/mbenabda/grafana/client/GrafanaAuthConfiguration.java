package com.mbenabda.grafana.client;

import okhttp3.OkHttpClient;

public abstract class GrafanaAuthConfiguration {
    abstract OkHttpClient.Builder authorize(OkHttpClient.Builder builder);
}
