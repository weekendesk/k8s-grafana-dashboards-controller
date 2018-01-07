# k8s-grafana-dashboards-controller

Kubernetes controller that watches dashboard configurations defined as configmaps and adds/updates/deletes them to/from grafana


# Caveat
Because grafana API uses title-derived slugs to identify dashboards, and in order not to have to manage state for this controller: 
once a dashboard is managed, its title must not be changed.

To update a managed dashboard's title: 
- delete the corresponding configmap from kubernetes
- change the title in the configmap's dashboard json desciption
- apply the configmap manifest


# TODO
- parse args
    - k8s:
    ```--kubeconfig```,
    ```--cluster```,
    ```--context```,
    ```--selector```,
    ```--namespace```,
    ```--all-namespaces```
    - grafana: 
    ```--grafana-url```,
    ```--grafana-api-key```
- tests
