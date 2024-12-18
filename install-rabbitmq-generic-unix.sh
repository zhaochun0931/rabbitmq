https://www.rabbitmq.com/docs/install-generic-unix






wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v4.0.5/rabbitmq-server-generic-unix-4.0.5.tar.xz
tar -xf rabbitmq-server-generic-unix-4.0.5.tar.xz


cat << done >> ~/.bash_profile
export PATH=/root/rabbitmq_server-4.0.5/sbin:$PATH
done





/root/rabbitmq_server-4.0.5/etc/rabbitmq/rabbitmq.conf


# run rabbitmq server in the backgound
rabbitmq-server -detached
rabbitmqctl shutdown








rabbitmq-plugins enable rabbitmq_management








