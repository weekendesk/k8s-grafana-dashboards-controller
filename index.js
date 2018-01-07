const Controller = require('./controller.js');
const Grafana = require('./grafana.js');
const ResourceWatcher = require('./k8sResourceWatcher.js');
const Kubernetes = require('kubernetes-client');

new Controller(
    new ResourceWatcher(
        new Kubernetes.Core(Kubernetes.config.getInCluster()).namespaces.configmap,
        process.env.CONFIGMAP_SELECTOR
    ),
    new Grafana({
        url: process.env.GRAFANA_API_URL,
        apiKey: process.env.GRAFANA_API_KEY
    })
).run();