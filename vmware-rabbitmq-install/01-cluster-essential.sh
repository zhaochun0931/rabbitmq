
# MUST CHANGE THE USERNAME AND THE PASSWORD BEFORE INSTALLING THIS  FILE
# MUST CHANGE THE USERNAME AND THE PASSWORD BEFORE INSTALLING THIS  FILE
# MUST CHANGE THE USERNAME AND THE PASSWORD BEFORE INSTALLING THIS  FILE



tar -xzf tanzu-cluster-essentials-linux-amd64-1.4.1.tgz
export INSTALL_BUNDLE=registry.tanzu.vmware.com/tanzu-cluster-essentials/cluster-essentials-bundle@sha256:2354688e46d4bb4060f74fca069513c9b42ffa17a0a6d5b0dbb81ed52242ea44


tar -xzf tanzu-cluster-essentials-linux-amd64-1.7.1.tgz
export INSTALL_BUNDLE=registry.tanzu.vmware.com/tanzu-cluster-essentials/cluster-essentials-bundle@sha256:ca8584ff2ad4a4cf7a376b72e84fd9ad84ac6f38305767cdfb12309581b521f5






export INSTALL_REGISTRY_HOSTNAME=registry.tanzu.vmware.com
export INSTALL_REGISTRY_USERNAME=TANZU-NET-USER
export INSTALL_REGISTRY_PASSWORD=TANZU-NET-PASSWORD

./install.sh --yes




