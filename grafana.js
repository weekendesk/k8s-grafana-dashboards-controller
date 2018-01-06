const axios = require("axios");

// https://github.com/tsloughter/grafana-operator/blob/master/pkg/grafana/grafana.go

class Grafana {
    constructor(url) {
        this.client = axios.create({
            baseURL: url,
            timeout: 1000
        });
    }

    createDashboard(configmap) {
        console.log(JSON.parse(configmap.data[Object.keys(configmap.data)[0]]));
        return Promise.resolve();
    }

    updateDashboard(configmap) {
        console.log(JSON.parse(configmap.data[Object.keys(configmap.data)[0]]));
        return Promise.resolve();
    }

    deleteDashboard(configmap) {
        console.log(JSON.parse(configmap.data[Object.keys(configmap.data)[0]]));
        return Promise.resolve();
    }

    createDatasource(configmap) {
        console.log(JSON.parse(configmap.data[Object.keys(configmap.data)[0]]));
        return Promise.resolve();
    }

    updateDatasource(datasource) {
        console.log(JSON.parse(configmap.data[Object.keys(configmap.data)[0]]));
        return Promise.resolve();
    }

    deleteDatasource(datasource) {
        console.log(JSON.parse(configmap.data[Object.keys(configmap.data)[0]]));
        return Promise.resolve();
    }
}

module.exports = Grafana;