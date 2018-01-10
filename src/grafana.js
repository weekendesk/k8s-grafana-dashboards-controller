const axios = require("axios");

const unwrapResponse = (response) => response.data;

// removes the db/ prefix from the uri
const asSlug = (dashboardUri) => dashboardUri.substring(3, dashboardUri.length);
const authConfig = (auth) => {
    if (auth.apiKey) {
        return {
            headers: { 'Authorization': 'Bearer ' + auth.apiKey }
        };
    }

    const { basic } = auth;
    if (basic.username && basic.password) {
        return {
            auth: basic
        };
    }

    return {}
}

class Grafana {
    constructor({ url = "", auth = {} }) {
        this.client = axios.create(
            Object.assign({
                    baseURL: url
                },
                authConfig(auth)
            )
        );
    }

    createDashboard(dashboard) {
        return this.client
            .post("dashboards/import", dashboard)
            .then(unwrapResponse);
    }

    updateDashboard({ dashboard }) {
        return this.client
            .post("dashboards/db", { dashboard })
            .then(unwrapResponse);
    }

    deleteDashboard(slug) {
        return this.client.delete("dashboards/db/" + slug);
    }

    slug(json) {
        const title = json.dashboard.title;

        return this.client.get(
                "search", {
                    params: {
                        query: title
                    }
                })
            .then(unwrapResponse)
            .then(results => {
                if (results.length !== 1) {
                    throw new Error("Expected to find a grafana dashboard with title \"" + title + "\", found " + results.length + ": " + JSON.stringify(results, null, 2));
                }

                return asSlug(results[0].uri);
            });
    }
}

module.exports = Grafana;