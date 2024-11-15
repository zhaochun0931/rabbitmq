helm registry login rabbitmq-helmoci.packages.broadcom.com --username='username' --password='password'






kubectl create ns rabbitmq-system

kubectl create secret docker-registry tanzu-rabbitmq-registry-creds --docker-server "rabbitmq.packages.broadcom.com" --docker-username "username" --docker-password "password" -n rabbitmq-system

kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.5.3/cert-manager.yaml

helm install tanzu-rabbitmq oci://rabbitmq-helmoci.packages.broadcom.com/tanzu-rabbitmq-operators --namespace rabbitmq-system

kubectl get all -n rabbitmq-system


helm list --namespace rabbitmq-system





# you can use the kubectl to deploy a rabbitmq cluster












# how to install configure the tls in the pod

https://www.rabbitmq.com/kubernetes/operator/using-operator#tls

kubectl create secret tls tls-secret --cert=server.pem --key=server-key.pem


