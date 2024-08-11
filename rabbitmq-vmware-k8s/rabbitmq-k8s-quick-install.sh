echo
echo
echo
echo "Please install the tanzu cluster esstential first!!!"
echo 
echo 
echo 

echo "Please upload the tanzu cluster essential"

read -p "input the tanzu net username:" username
read -p "input the tanzu net password:" password
read -p "input the ip of the upstream:" ip
read -p "input the number of the replicas:" replicano

echo "installing...\n"

wget https://raw.githubusercontent.com/zhaochun0931/rabbitmq/main/rabbitmq-vmware-k8s/01-cluster-essential.sh
sed -i "s/tanzu-net-username/$username/g" 01-cluster-essential.sh
sed -i "s/tanzu-net-password/$password/g" 01-cluster-essential.sh
bash 01-cluster-essential.sh

wget https://raw.githubusercontent.com/zhaochun0931/rabbitmq/main/rabbitmq-vmware-k8s/02-secret.yaml
sed -i "s/tanzu-net-username/$username/g" 02-secret.yaml
sed -i "s/tanzu-net-password/$password/g" 02-secret.yaml
kubectl apply -f 02-secret.yaml



kubectl apply -f https://raw.githubusercontent.com/zhaochun0931/rabbitmq/main/rabbitmq-vmware-k8s/03-packageRepository.yaml


kubectl apply -f https://raw.githubusercontent.com/zhaochun0931/rabbitmq/main/rabbitmq-vmware-k8s/04-serviceAccount.yaml


kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.5.3/cert-manager.yaml

kubectl get pods -n cert-manager


sleep 10



kubectl apply -f https://raw.githubusercontent.com/zhaochun0931/rabbitmq/main/rabbitmq-vmware-k8s/06-packageInstall.yaml




kubectl get packages


kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml
kubectl annotate storageclass local-path storageclass.kubernetes.io/is-default-class=true


wget https://raw.githubusercontent.com/zhaochun0931/rabbitmq/main/rabbitmq-vmware-k8s/rabbitmq1.yaml
wget https://raw.githubusercontent.com/zhaochun0931/rabbitmq/main/rabbitmq-vmware-k8s/rabbitmq2.yaml



sed -i "s/1.1.1.1/$ip/g" rabbitmq1.yaml
sed -i "s/1.1.1.1/$ip/g" rabbitmq2.yaml

sed -i "s/replicas: 3/replicas: $replicano/g" rabbitmq1.yaml
sed -i "s/replicas: 3/replicas: $replicano/g" rabbitmq2.yaml


echo -e "\n\n\nInstallation successfully! \nYou can deloy Rabbitmq cluster now."

echo -e "run below command on cluster1:\nkubectl apply -f rabbitmq1.yaml"
echo
echo -e "run below command on cluster2:\nkubectl apply -f rabbitmq2.yaml"
