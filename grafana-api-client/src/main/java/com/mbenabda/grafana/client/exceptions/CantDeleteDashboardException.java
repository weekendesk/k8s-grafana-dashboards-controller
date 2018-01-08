package com.mbenabda.grafana.client.exceptions;

import okhttp3.ResponseBody;

import java.io.IOException;

public class CantDeleteDashboardException extends GrafanaException {

    public static CantDeleteDashboardException withErrorBody(ResponseBody body)
            throws IOException {
        return body != null
                ? new CantDeleteDashboardException("Grafana error: " + body.string())
                : new CantDeleteDashboardException("Grafana error");
    }

    public CantDeleteDashboardException(String message) {
        super(message);
    }
}
