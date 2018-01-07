const axios = require("axios");

const unwrapResponse = (response) => response.data;

class Grafana {
    constructor({ url = "", apiKey = null }) {
        this.client = axios.create({
            baseURL: url,
            timeout: 1000,
            headers: !!apiKey ? {
                'Authorization': 'Bearer ' + apiKey
            } : {}
        });
    }

    createDashboard(dashboard) {
        return this.client
            .post("dashboards/db", Object.assign({}, dashboard, { overwrite: false }))
            .then(unwrapResponse);
    }

    updateDashboard(dashboard) {
        return this.client
            .post("dashboards/db", Object.assign({}, dashboard, { overwrite: true }))
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
                // remove the db/ prefix from the uri
                const dashboardUri = results[0].uri;
                return dashboardUri.substring(3, dashboardUri.length);
            });
    }
}

module.exports = Grafana;