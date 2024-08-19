kubectl create secret tls tls-secret --cert=tls.crt --key=tls.key -n rabbitmq-system



kubectl apply -f certificate.yaml



kubectl get secret tls-secret -n rabbitmq-system -o jsonpath='{.data.ca\.crt}' | base64 --decode | openssl x509 -text -noout











# the Secret must be stored with key 'ca.crt'.
kubectl create secret generic ca-secret --from-file=ca.crt=/root/ca-cert.pem

kubectl create secret tls tls-secret --cert=/root/server-cert.pem --key=/root/server-key.pem






$ kubectl get pods -n rabbitmq-system
NAME                                          READY   STATUS    RESTARTS      AGE
messaging-topology-operator-f549f645c-v69cl   1/1     Running   1 (11h ago)   7d11h
rabbitmq-cluster-operator-7c9f55c7f7-4g7n7    1/1     Running   2 (11h ago)   7d11h
rabbitmq1-server-0                            1/1     Running   0             3m11s
$




$ kubectl get svc -n rabbitmq-system
NAME              TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)                        AGE
rabbitmq1         ClusterIP   10.96.194.32    <none>        5672/TCP,15672/TCP,15692/TCP   67s
rabbitmq1-nodes   ClusterIP   None            <none>        4369/TCP,25672/TCP             67s
$






$ kubectl exec -it rabbitmq1-server-0 -n rabbitmq-system -- bash
Defaulted container "rabbitmq" out of: rabbitmq, setup-container (init)
rabbitmq@rabbitmq1-server-0:/$ hostname
rabbitmq1-server-0
rabbitmq@rabbitmq1-server-0:/$ cat /etc/resolv.conf
nameserver 10.96.0.10
search rabbitmq-system.svc.cluster.local svc.cluster.local cluster.local
options ndots:5
rabbitmq@rabbitmq1-server-0:/$ 
rabbitmq@rabbitmq1-server-0:/$





kubectl exec -it rabbitmq1-server-0 -n rabbitmq-system -- openssl s_client -connect rabbitmq1-nodes.rabbitmq-system.svc.cluster.local:5671 </dev/null


