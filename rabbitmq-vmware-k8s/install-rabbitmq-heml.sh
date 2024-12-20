# please install helm





export USERNAME="username"
export PASSWORD="password"



helm registry login rabbitmq-helmoci.packages.broadcom.com --username=$USERNAME --password=$PASSWORD













kubectl create ns rabbitmq-system


kubectl create secret docker-registry tanzu-rabbitmq-registry-creds --docker-server "rabbitmq.packages.broadcom.com" --docker-username $USERNAME --docker-password $PASSWORD -n rabbitmq-system

# kubectl create secret docker-registry tanzu-rabbitmq-registry-creds --docker-server "rabbitmq.packages.broadcom.com" --docker-username "username" --docker-password "password" -n rabbitmq-system





kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.5.3/cert-manager.yaml

helm install tanzu-rabbitmq oci://rabbitmq-helmoci.packages.broadcom.com/tanzu-rabbitmq-operators --namespace rabbitmq-system

kubectl get all -n rabbitmq-system


helm list --namespace rabbitmq-system







# you can use the kubectl to deploy a rabbitmq cluster






# how to connect the cluster

nohup kubectl port-forward service/my-rabbitmq -n rabbitmq-system --address 0.0.0.0 15672:15672 5672:5672 &

username="$(kubectl get secret my-rabbitmq-default-user -n rabbitmq-system -o jsonpath='{.data.username}' | base64 --decode)"
password="$(kubectl get secret my-rabbitmq-default-user -n rabbitmq-system -o jsonpath='{.data.password}' | base64 --decode)"
echo "username: $username"
echo "password: $password"











# how to install configure the tls in the pod

https://www.rabbitmq.com/kubernetes/operator/using-operator#tls

kubectl create secret tls tls-secret --cert=server.pem --key=server-key.pem


