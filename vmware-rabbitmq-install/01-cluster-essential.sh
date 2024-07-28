
# MUST CHANGE THE USERNAME AND THE PASSWORD BEFORE INSTALLING THIS  FILE
# MUST CHANGE THE USERNAME AND THE PASSWORD BEFORE INSTALLING THIS  FILE
# MUST CHANGE THE USERNAME AND THE PASSWORD BEFORE INSTALLING THIS  FILE



# tar -xzf tanzu-cluster-essentials-linux-amd64-1.4.1.tgz
# export INSTALL_BUNDLE=registry.tanzu.vmware.com/tanzu-cluster-essentials/cluster-essentials-bundle@sha256:2354688e46d4bb4060f74fca069513c9b42ffa17a0a6d5b0dbb81ed52242ea44


tar -xzf tanzu-cluster-essentials-linux-amd64-1.7.1.tgz
export INSTALL_BUNDLE=registry.tanzu.vmware.com/tanzu-cluster-essentials/cluster-essentials-bundle@sha256:ca8584ff2ad4a4cf7a376b72e84fd9ad84ac6f38305767cdfb12309581b521f5




# v1.9



export MY_BROADCOM_SUPPORT_ACCESS_TOKEN=password
export INSTALL_BUNDLE=cluster-essentials.packages.broadcom.com/tanzu-cluster-essentials/cluster-essentials-bundle@sha256:678c20e14e1065c6a97828632d02b0716ef453e2c9b3c5e1ea0dba1817bd8125
export INSTALL_REGISTRY_HOSTNAME=cluster-essentials.packages.broadcom.com
export INSTALL_REGISTRY_USERNAME=xxx@broadcom.com
export INSTALL_REGISTRY_PASSWORD=${MY_BROADCOM_SUPPORT_ACCESS_TOKEN}

./install.sh --yes




After installation, you can verify that Tanzu Cluster Essentials is installed correctly by checking the relevant pods are running. Run this command:

kubectl get all -n kapp-controller
kubectl get all -n secretgen-controller



You should see that the STATUS is running for the kapp-controller and secretgen-controller pods.


