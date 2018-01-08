package com.mbenabda.grafana.client;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface GrafanaService {
    String DASHBOARDS = "api/dashboards/db/";
    String SEARCH = "api/search";
    String IMPORT = "api/dashboards/import";

    @POST(IMPORT)
    Call<JsonNode> importDashboard(@Body JsonNode dashboardJson);

    @DELETE(DASHBOARDS + "{slug}")
    Call<JsonNode> deleteDashboard(@Path("slug") String slug);

    @GET(SEARCH)
    Call<List<JsonNode>> searchDashboard(@Query("query") String title);
}
