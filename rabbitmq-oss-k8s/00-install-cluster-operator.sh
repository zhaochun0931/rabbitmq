# install cluster operator
kubectl apply -f "https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml"
kubectl get customresourcedefinitions.apiextensions.k8s.io | grep rabbitmqclusters.rabbitmq.com








# install message topology operator
kubectl apply -f https://github.com/rabbitmq/messaging-topology-operator/releases/latest/download/messaging-topology-operator-with-certmanager.yaml





# up till now, you can deploy rabbitmq cluster

