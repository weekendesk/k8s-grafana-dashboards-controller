Kubernetes controller that watches dashboard configurations defined as configmaps and adds/updates/deletes them to/from grafana

Kubernetes
=======
- Access to the API is expected to be granted by the `ServiceAccount` attached to the pod running the controller's container.
- You can use kube-api compatible [labelSelector](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#label-selectors)s to select the configmaps to watch for dashboard descriptions.

Grafana
=======
- The controller supports either `api key` or `basic auth`

Configuration
=======

Env Variable | | Description | Default | Example
--- | --- | --- | --- | ---
`CONFIGMAP_SELECTOR` | optional | kube-api compatible [labelSelector](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#label-selectors) | `""` | `"role=grafana-dashboard,app=awesome-app"`
`GRAFANA_API_URL` | `required` | Grafana's api base URL | `null` | `http://grafana.monitoring.svc.cluster.local/api/`
`GRAFANA_API_KEY` | `required` unless using basic auth | Grafana API Key (get one at `<YOU-GRAFANA-INSTANCE-URL>`/org/apikeys) | `null` | `"eyJrIjoiWlc4VjZaaFlZbWhwdzFiNVlHbXRn....."`
`GRAFANA_BASIC_AUTH_USERNAME` | `required` if using basic auth | Grafana username | `null` | `"mbenabda"`
`GRAFANA_BASIC_AUTH_PASSWORD` | `required` if using basic auth | Grafana plain text password | `null` | `"1234"`


Caveat
=======
Because grafana API uses title-derived slugs to identify dashboards, and in order not to have to manage state for this controller: 
once a dashboard is managed, its title must not be changed.

To update a managed dashboard's title: 
- delete the corresponding configmap from kubernetes
- change the title in the configmap's dashboard json desciption
- apply the configmap manifest


TODO
=======
- better logs (timestamp, levels)
- parse args
    - k8s:
    `--kubeconfig`,
    `--cluster`,
    `--context`,
    `--selector`,
    `--namespace`,
    `--all-namespaces`
    - grafana (api url + auth)

- unit tests
