const key = (configmap) => [configmap.metadata.namespace, configmap.metadata.name].join("/");
const unwrapDashboard = (configmap) => JSON.parse(configmap.data.json);

class Controller {
    constructor(configMapsWatcher, grafana) {
        this.configMapsWatcher = configMapsWatcher;
        this.grafana = grafana;
    }

    run() {
        this.configMapsWatcher.watch({
            onAdded: this.handleConfigMapCreated.bind(this),
            onModified: this.handleConfigMapModified.bind(this),
            onDeleted: this.handleConfigMapDeleted.bind(this),
            onUnhandledError: console.error
        });
    }

    handleConfigMapCreated(configmap) {
        this.grafana
            .createDashboard(unwrapDashboard(configmap))
            .then((result) => {
                console.log("created dashboard for configmap", key(configmap), result);
            })
            .catch((error) => {
                console.error("unable to create dashboard for configmap", key(configmap), error);
            });
    }

    handleConfigMapModified(configmap) {
        this.grafana
            .updateDashboard(unwrapDashboard(configmap))
            .then((result) => {
                console.log("updated dashboard for configmap", key(configmap), result);
            })
            .catch((error) => {
                console.error("unable to update dashboard for configmap", key(configmap), error);
            });
    }

    handleConfigMapDeleted(configmap) {
        this.grafana
            .slug(unwrapDashboard(configmap))
            .then(this.grafana.deleteDashboard.bind(this.grafana))
            .then(() => {
                console.log("the dashboard managed by configmap", key(configmap), "has been deleted");
            })
            .catch((error) => {
                console.error("unable to delete dashboard for configmap", key(configmap), error);
            });
    }
}

module.exports = Controller;