package com.mbenabda.grafana.client.exceptions;

import okhttp3.ResponseBody;

import java.io.IOException;

public class GrafanaException extends Exception {

    public static GrafanaException withErrorBody(ResponseBody body) throws IOException {
        return body != null
                ? new GrafanaException("Grafana error: " + body.string())
                : new GrafanaException("Grafana error");
    }

    public GrafanaException(String message) {
        super(message);
    }

}
