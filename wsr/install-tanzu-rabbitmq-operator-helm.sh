# install helm


curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh


helm version







# install cert-manager

kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.16.2/cert-manager.yaml




export USERNAME="username"
export PASSWORD="password"
helm registry login rabbitmq-helmoci.packages.broadcom.com --username=$USERNAME --password=$PASSWORD








kubectl create ns rabbitmq-system

kubectl create secret docker-registry tanzu-rabbitmq-registry-creds --docker-server "rabbitmq.packages.broadcom.com" \
--docker-username $USERNAME \
--docker-password $PASSWORD -n rabbitmq-system

# kubectl create secret docker-registry tanzu-rabbitmq-registry-creds --docker-server "rabbitmq.packages.broadcom.com" --docker-username "username" --docker-password "password" -n rabbitmq-system




helm install tanzu-rabbitmq oci://rabbitmq-helmoci.packages.broadcom.com/tanzu-rabbitmq-operators --namespace rabbitmq-system

kubectl get secret -A

kubectl get all -n rabbitmq-system

helm list --namespace rabbitmq-system







echo "Now you can use the kubectl to deploy a rabbitmq cluster"






# how to connect the cluster

nohup kubectl port-forward service/rabbitmq1 -n rabbitmq-system --address 0.0.0.0 15672:15672 5672:5672 5552:5552 &

username="$(kubectl get secret my-rabbitmq-default-user -n rabbitmq-system -o jsonpath='{.data.username}' | base64 --decode)"
password="$(kubectl get secret my-rabbitmq-default-user -n rabbitmq-system -o jsonpath='{.data.password}' | base64 --decode)"
echo "username: $username"
echo "password: $password"











# how to install configure the tls in the pod

https://www.rabbitmq.com/kubernetes/operator/using-operator#tls

kubectl create secret tls tls-secret --cert=server.pem --key=server-key.pem
