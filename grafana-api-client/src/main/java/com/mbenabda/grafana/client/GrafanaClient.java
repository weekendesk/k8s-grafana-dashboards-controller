package com.mbenabda.grafana.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.mbenabda.grafana.client.exceptions.GrafanaException;

import java.io.IOException;

public interface GrafanaClient {
    void importDashboard(JsonNode dashboard) throws GrafanaException, IOException;

    void deleteDashboard(String slug) throws GrafanaException, IOException;

    JsonNode searchDashboard(String title) throws GrafanaException, IOException;

    String slug(String title) throws GrafanaException, IOException;
}
