kubectl apply -f https://raw.githubusercontent.com/zhaochun-vmware/rabbitmq/main/vmware-rabbitmq-install/03-packageRepository.yaml


kubectl apply -f https://raw.githubusercontent.com/zhaochun-vmware/rabbitmq/main/vmware-rabbitmq-install/04-serviceAccount.yaml


kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.5.3/cert-manager.yaml

kubectl apply -f https://raw.githubusercontent.com/zhaochun-vmware/rabbitmq/main/vmware-rabbitmq-install/06-packageInstall.yaml




kubectl get packages


kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/odeploy/local-path-storage.yaml

kubectl annotate storageclass local-path storageclass.kubernetes.io/is-default-class=true
