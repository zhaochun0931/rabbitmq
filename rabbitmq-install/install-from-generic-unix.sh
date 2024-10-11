wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.13.7/rabbitmq-server-generic-unix-3.13.7.tar.xz



tar -xf rabbitmq-server-generic-unix-3.13.7.tar.xz


cat << done >> ~/.bash_profile
export PATH=/root/rabbitmq_server-3.13.7/sbin:$PATH
done



# run rabbitmq server in the backgound
rabbitmq-server -detached





rabbitmq-server -detached




rabbitmqctl shutdown
