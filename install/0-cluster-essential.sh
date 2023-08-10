
# MUST CHANGE THE USERNAME AND THE PASSWORD BEFORE INSTALLING THIS  FILE
# MUST CHANGE THE USERNAME AND THE PASSWORD BEFORE INSTALLING THIS  FILE
# MUST CHANGE THE USERNAME AND THE PASSWORD BEFORE INSTALLING THIS  FILE



tar -xzf tanzu-cluster-essentials-linux-amd64-1.4.1.tgz

$ tar -xzvf tanzu-cluster-essentials-linux-amd64-1.4.1.tgz
x install.sh
x uninstall.sh
x imgpkg
x kbld
x kapp
x ytt
$

export INSTALL_BUNDLE=registry.tanzu.vmware.com/tanzu-cluster-essentials/cluster-essentials-bundle@sha256:2354688e46d4bb4060f74fca069513c9b42ffa17a0a6d5b0dbb81ed52242ea44
export INSTALL_REGISTRY_HOSTNAME=registry.tanzu.vmware.com
export INSTALL_REGISTRY_USERNAME=TANZU-NET-USER
export INSTALL_REGISTRY_PASSWORD=TANZU-NET-PASSWORD

./install.sh --yes




