# install helm








# install cert-manager

kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.16.2/cert-manager.yaml


export USERNAME="username"
export PASSWORD="password"
helm registry login rabbitmq-helmoci.packages.broadcom.com --username=$USERNAME --password=$PASSWORD








kubectl create ns rabbitmq-system

# Provide imagePullSecrets
kubectl create secret docker-registry tanzu-rabbitmq-registry-creds --docker-server "rabbitmq.packages.broadcom.com" \
--docker-username $USERNAME \
--docker-password $PASSWORD -n rabbitmq-system



# kubectl create secret docker-registry tanzu-rabbitmq-registry-creds --docker-server "rabbitmq.packages.broadcom.com" --docker-username "username" --docker-password "password" -n rabbitmq-system



# Install the Tanzu RabbitMQ operators
helm install tanzu-rabbitmq oci://rabbitmq-helmoci.packages.broadcom.com/tanzu-rabbitmq-operators --namespace rabbitmq-system

kubectl get secret -A
kubectl get all -n rabbitmq-system
helm list --namespace rabbitmq-system







echo "Now you can use the kubectl to deploy a rabbitmq cluster"






