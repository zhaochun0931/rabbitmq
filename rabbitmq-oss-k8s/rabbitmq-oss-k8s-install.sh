# install the open source rabbitmq in K8s


# install k8s cluster











# Up till now, you can deploy rabbitmq cluster





















# deploy the rabbitmq cluster with rabbitmq cluster operator

kubectl apply -f rabbitmq.yaml



# Up till now, the rabbitmq cluster is up.

the /var/lib/rabbitmq/.erlang.cookie should be identical within the same cluster.

# the erlang.cookie was saved in the secret


kubectl get secrets
kubectl get rabbitmqcluster










/etc/hosts


$ cat /etc/hosts
# Kubernetes-managed hosts file.
127.0.0.1	localhost
::1	localhost ip6-localhost ip6-loopback
fe00::0	ip6-localnet
fe00::0	ip6-mcastprefix
fe00::1	ip6-allnodes
fe00::2	ip6-allrouters
10.85.0.9	hello-world-server-1.hello-world-nodes.default.svc.cluster.local	hello-world-server-1
$

















# how to access Rabbitmq cluster
username="$(kubectl get secret my-rabbitmq-default-user -o jsonpath='{.data.username}' | base64 --decode)"
password="$(kubectl get secret my-rabbitmq-default-user -o jsonpath='{.data.password}' | base64 --decode)"

echo "username: $username"
echo "password: $password"

nohup kubectl port-forward service/my-rabbitmq --address 0.0.0.0 15672:15672 5672:5672 &




