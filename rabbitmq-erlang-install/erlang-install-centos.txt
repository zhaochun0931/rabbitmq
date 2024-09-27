# install erlang and rabbitmq on centos


wget https://github.com/rabbitmq/erlang-rpm/releases/download/v26.2.5.3/erlang-26.2.5.3-1.el9.aarch64.rpm
wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v4.0.2/rabbitmq-server-4.0.2-1.el8.noarch.rpm



rpm -ivh erlang-26.2.5.3-1.el9.aarch64.rpm
rpm -ivh rabbitmq-server-4.0.2-1.el8.noarch.rpm






/var/lib/rabbitmq/.erlang.cookie




systemctl enable rabbitmq-server


systemctl start rabbitmq-server
