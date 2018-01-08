package com.mbenabda.grafana.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.net.InetSocketAddress;

import static java.lang.String.format;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.verify.VerificationTimes.once;

public class GrafanaClientImplTest {

    private static final String API_KEY = "my api key";
    private static final String DASHBOARD_SLUG = "slug";
    private static final String DASHBOARD_TITLE = "title";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private JsonNode DASHBOARD;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    private MockServerClient grafanaAPI;
    private GrafanaClient grafanaClient;

    @Before
    public void setup() throws Exception {
        grafanaAPI = mockServerRule.getClient();
        DASHBOARD = new ObjectMapper().readTree("{\"id\": \"some dashboard id\"}");
        grafanaClient = clientFor(grafanaAPI.remoteAddress());
    }

    @Test
    public void should_import_dashboard() throws Exception {
        grafanaAPI.when(request())
                .respond(success());

        grafanaClient.importDashboard(DASHBOARD);

        grafanaAPI.verify(
                authenticated(
                        request()
                                .withPath("/api/dashboards/import")
                                .withMethod(POST)
                                .withHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
                                .withBody(json(DASHBOARD))
                ),
                once()
        );
    }

    @Test
    public void should_delete_dashboard() throws Exception {
        grafanaAPI.when(request())
                .respond(success());

        grafanaClient.deleteDashboard(DASHBOARD_SLUG);

        grafanaAPI.verify(
                authenticated(
                        request()
                                .withPath(format("/api/dashboards/db/%s", DASHBOARD_SLUG))
                                .withMethod(DELETE)
                ),
                once()
        );
    }

    @Test
    public void should_search_dashboards() throws Exception {
        grafanaAPI.when(request())
                .respond(searchResults(DASHBOARD));

        grafanaClient.searchDashboard(DASHBOARD_TITLE);

        grafanaAPI.verify(
                authenticated(
                        request()
                                .withPath("/api/search")
                                .withQueryStringParameter("query", DASHBOARD_TITLE)
                                .withMethod(GET)
                ),
                once()
        );
    }

    private HttpResponse searchResults(JsonNode... dashboards) {
        return response()
                .withStatusCode(OK_200.code())
                .withBody(json(dashboards));
    }

    private HttpRequest authenticated(HttpRequest request) {
        return request.clone()
                .withHeader(authHeader())
                .withKeepAlive(true)
                .withSecure(false);
    }

    private GrafanaClient clientFor(InetSocketAddress serverAddr) {
        return new GrafanaClientImpl(
                new GrafanaConfiguration(
                        format("http://%s:%d", serverAddr.getHostName(), serverAddr.getPort()),
                        new ApiKeyAuthConfiguration(API_KEY)
                )
        );
    }

    private HttpResponse success() {
        return response()
                .withStatusCode(OK_200.code())
                .withBody(json("{}"));
    }

    private Header authHeader() {
        return Header.header("Authorization", "Bearer " + API_KEY);
    }
}