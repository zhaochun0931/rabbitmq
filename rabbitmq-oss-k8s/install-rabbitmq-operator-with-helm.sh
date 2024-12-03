helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install my-release bitnami/rabbitmq-cluster-operator


# The last command deploys the RabbitMQ Cluster Kubernetes Operator on the Kubernetes cluster in the default configuration.

