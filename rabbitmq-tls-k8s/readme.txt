kubectl create secret tls tls-secret --cert=tls.crt --key=tls.key



kubectl apply -f certificate.yaml




















# the Secret must be stored with key 'ca.crt'.
kubectl create secret generic ca-secret --from-file=ca.crt=/root/ca-cert.pem

kubectl create secret tls tls-secret --cert=/root/server-cert.pem --key=/root/server-key.pem




