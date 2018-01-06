const configmap = (configmapData) => JSON.parse(configmapData)
const matchesSelector = ({ labels = {} }) => true
const previousVersionMatchedSelector = () => false
const configmapId = (configmap) => [configmap.metadata.namespace, configmap.metadata.name].join("/")

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
        if (matchesSelector(configmap)) {
            this.grafana
                .createDashboard(configmap)
                .then(() => {
                    console.log("created dashboard for configmap", configmapId(configmap));
                })
                .catch((error) => {
                    console.error("unable to create dashboard for configmap", configmapId(configmap), error);
                });
        }
    }

    handleConfigMapModified(configmap) {
        // changes made the configmap match the selector
        if (matchesSelector(configmap) && !previousVersionMatchedSelector(configmap)) {
            this.handleConfigMapCreated(configmap)
        } else if (matchesSelector(configmap)) {
            this.grafana
                .updateDashboard(configmap)
                .then(() => {
                    console.log("updated dashboard for configmap", configmapId(configmap));
                })
                .catch((error) => {
                    console.error("unable to update dashboard for configmap", configmapId(configmap), error);
                });
        } // changes made the configmap not match the selector anymore
        else if (previousVersionMatchedSelector(configmap)) {
            this.handleConfigMapDeleted(configmap);
        }
    }

    handleConfigMapDeleted(configmap) {
        if (matchesSelector(configmap)) {
            this.grafana
                .deleteDashboard(configmap)
                .then(() => {
                    console.log("deleted dashboard for configmap", configmapId(configmap));
                })
                .catch((error) => {
                    console.error("unable to delete dashboard for configmap", configmapId(configmap), error);
                });
        }
    }
}

module.exports = Controller;