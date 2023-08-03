# install the latest erlang 
add-apt-repository ppa:rabbitmq/rabbitmq-erlang
apt update
apt install erlang


# install the default erlang
apt update

apt install curl software-properties-common apt-transport-https lsb-release -y

curl -fsSL https://packages.erlang-solutions.com/ubuntu/erlang_solutions.asc | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/erlang.gpg

echo "deb https://packages.erlang-solutions.com/ubuntu $(lsb_release -cs) contrib" | sudo tee /etc/apt/sources.list.d/erlang.list

apt update

apt install erlang -y










# install rabbitmq

curl -s https://packagecloud.io/install/repositories/rabbitmq/rabbitmq-server/script.deb.sh | sudo bash

apt update

apt install rabbitmq-server -y




# The RabbitMQ server gives you a default username and password, that is guest:guest. Note that guest:guest won't work for remote a RabbitMQ server later than version 3.3
# add user and assign the permssion 

rabbitmqctl add_user admin password
rabbitmqctl set_user_tags admin administrator



# grant the user permission to modify, write, and read all vhosts
rabbitmqctl set_permissions --vhost / admin ".*" ".*" ".*"



rabbitmq-plugins enable rabbitmq_management

