const identifier = (configmap) => [configmap.metadata.namespace, configmap.metadata.name].join("/");
const dashboardManifest = (configmap) => JSON.parse(configmap.data.json);

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
            .createDashboard(dashboardManifest(configmap))
            .then((result) => {
                console.log("created dashboard for configmap", identifier(configmap), result);
            })
            .catch((error) => {
                console.error("unable to create dashboard for configmap", identifier(configmap), error);
            });
    }

    handleConfigMapModified(configmap) {
        this.grafana
            .updateDashboard(dashboardManifest(configmap))
            .then((result) => {
                console.log("updated dashboard for configmap", identifier(configmap), result);
            })
            .catch((error) => {
                console.error("unable to update dashboard for configmap", identifier(configmap), error);
            });
    }

    handleConfigMapDeleted(configmap) {
        this.grafana
            .slug(dashboardManifest(configmap))
            .then(this.grafana.deleteDashboard.bind(this.grafana))
            .then((deletion) => {
                console.log("deleted dashboard", deletion.title, "because configmap", identifier(configmap), "has been deleted");
            })
            .catch((error) => {
                console.error("unable to delete dashboard for configmap", identifier(configmap), error);
            });
    }
}

module.exports = Controller;