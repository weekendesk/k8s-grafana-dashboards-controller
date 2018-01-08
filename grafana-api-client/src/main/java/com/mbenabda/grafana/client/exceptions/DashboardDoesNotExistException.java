package com.mbenabda.grafana.client.exceptions;

public class DashboardDoesNotExistException extends GrafanaException {
    public DashboardDoesNotExistException(String message) {
        super(message);
    }
}
