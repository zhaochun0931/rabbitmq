kubectl create secret tls tls-secret --cert=tls.crt --key=tls.key



kubectl apply -f certificate.yaml





















 kubectl -n rabbitmq-system create secret generic rabbitmq-ca --from-file=ca.crt=/root/ca-cert.pem
