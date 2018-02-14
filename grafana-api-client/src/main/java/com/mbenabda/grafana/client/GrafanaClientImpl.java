package com.mbenabda.grafana.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbenabda.grafana.client.exceptions.CantDeleteDashboardException;
import com.mbenabda.grafana.client.exceptions.DashboardDoesNotExistException;
import com.mbenabda.grafana.client.exceptions.GrafanaException;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.util.concurrent.TimeUnit.SECONDS;

public class GrafanaClientImpl implements GrafanaClient {

    private final GrafanaService service;
    private static final Logger LOGGER = Logger.getLogger(GrafanaClientImpl.class.getName());

    private static final ObjectMapper mapper =
            new ObjectMapper()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    public GrafanaClientImpl(GrafanaConfiguration configuration) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(configuration.host())
                .client(configuration.auth()
                        .authorize(new OkHttpClient.Builder()
                                .writeTimeout(5, SECONDS)
                                .readTimeout(5, SECONDS)
                                .connectTimeout(1, SECONDS)
                        )
                        .build())
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        service = retrofit.create(GrafanaService.class);
    }

    public void importDashboard(JsonNode dashboard)
            throws GrafanaException, IOException {

        Response<JsonNode> response = service.importDashboard(dashboard).execute();

        if (!response.isSuccessful()) {
            throw GrafanaException.withErrorBody(response.errorBody());
        }
    }

    public void deleteDashboard(String slug)
            throws GrafanaException,
            IOException {

        Response<JsonNode> response = service.deleteDashboard(slug).execute();

        if (!response.isSuccessful()) {
            if (response.code() == HTTP_NOT_FOUND) {
                throw new DashboardDoesNotExistException("Dashboard " + slug + " does not exist");
            }

            throw CantDeleteDashboardException.withErrorBody(response.errorBody());
        }
    }

    public JsonNode searchDashboard(String title) throws GrafanaException, IOException {
        Response<List<JsonNode>> response = service.searchDashboard(title).execute();

        if (response.isSuccessful()) {
            if (response.body().size() == 1) {
                return response.body().get(0);
            } else {
                throw new DashboardDoesNotExistException(
                        format(
                                "Expected to find 1 dashboard with title %s, found %s",
                                title, response.body().size())
                );
            }
        } else if (response.code() == HTTP_NOT_FOUND) {
            throw new DashboardDoesNotExistException(
                    "Dashboard " + title + " does not exist");
        } else {
            throw GrafanaException.withErrorBody(response.errorBody());
        }
    }

    public String slug(String title) throws GrafanaException, IOException {
        return searchDashboard(title).get("uri").asText().substring(3);
    }

    @Override
    public boolean isAlive() {
        try {
            service.health().execute();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
