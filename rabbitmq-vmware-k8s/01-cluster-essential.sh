# v1.9.1


# please make sure the k8s cluster is ready

# do not use single or double quotation mark ' and " in the token

export MY_BROADCOM_SUPPORT_ACCESS_TOKEN=CLUSTER_ESSENTIAL_TOKEN
export INSTALL_BUNDLE=cluster-essentials.packages.broadcom.com/tanzu-cluster-essentials/cluster-essentials-bundle@sha256:678c20e14e1065c6a97828632d02b0716ef453e2c9b3c5e1ea0dba1817bd8125
export INSTALL_REGISTRY_HOSTNAME=cluster-essentials.packages.broadcom.com
export INSTALL_REGISTRY_USERNAME=username@broadcom.com
export INSTALL_REGISTRY_PASSWORD=${MY_BROADCOM_SUPPORT_ACCESS_TOKEN}
./install.sh --yes




# After installation, you can verify that Tanzu Cluster Essentials is installed correctly by checking the relevant pods are running. Run this command:
kubectl get all -n kapp-controller
kubectl get all -n secretgen-controller



# You should see that the STATUS is running for the kapp-controller and secretgen-controller pods.


