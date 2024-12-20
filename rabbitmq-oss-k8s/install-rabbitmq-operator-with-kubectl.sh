
- RabbitMQ Cluster Kubernetes Operator
- RabbitMQ Messaging Topology Operator
















# install RabbitMQ Cluster Operator

https://www.rabbitmq.com/kubernetes/operator/quickstart-operator


kubectl apply -f https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml
kubectl get all -n rabbitmq-system
kubectl get customresourcedefinitions.apiextensions.k8s.io






# install cert manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.15.2/cert-manager.yaml
kubectl get all -n cert-manager

# make sure the cert-manager pods are running





# install message topology operator
kubectl apply -f https://github.com/rabbitmq/messaging-topology-operator/releases/latest/download/messaging-topology-operator-with-certmanager.yaml
