
wget https://raw.githubusercontent.com/zhaochun-vmware/rabbitmq/main/vmware-rabbitmq-install/01-cluster-essential.sh

wget https://raw.githubusercontent.com/zhaochun-vmware/rabbitmq/main/vmware-rabbitmq-install/02-secret.yaml


kubectl apply -f https://raw.githubusercontent.com/zhaochun-vmware/rabbitmq/main/vmware-rabbitmq-install/03-packageRepository.yaml


kubectl apply -f https://raw.githubusercontent.com/zhaochun-vmware/rabbitmq/main/vmware-rabbitmq-install/04-serviceAccount.yaml


kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.5.3/cert-manager.yaml

kubectl apply -f https://raw.githubusercontent.com/zhaochun-vmware/rabbitmq/main/vmware-rabbitmq-install/06-packageInstall.yaml




kubectl get packages


kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml
kubectl annotate storageclass local-path storageclass.kubernetes.io/is-default-class=true



echo -e "\n\n\nInstallation successfully! \nYou can deloy Rabbitmq cluster now."
