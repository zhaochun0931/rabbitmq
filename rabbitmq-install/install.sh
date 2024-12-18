






# install rabbitmq

curl -s https://packagecloud.io/install/repositories/rabbitmq/rabbitmq-server/script.deb.sh | sudo bash

apt update

apt install rabbitmq-server -y




# enable management plugin
rabbitmq-plugins enable rabbitmq_management


# The RabbitMQ server gives you a default username and password, that is guest:guest. Note that guest:guest won't work for remote a RabbitMQ server later than version 3.3
# add user and assign the permssion 

rabbitmqctl add_user admin password
rabbitmqctl set_user_tags admin administrator



# grant the user permission to modify, write, and read all vhosts
rabbitmqctl set_permissions --vhost / admin ".*" ".*" ".*"





# rabbitmq interactive mode
export RABBITMQ_ALLOW_INPUT='true'

rabbit_log:debug("test").
rabbit_log:info("test").






/var/vcap/store/rabbitmq/.erlang.cookie
















# kill the erlang process manually

ps aux | grep beam
kill -9 PID















# log collection

sudo -i
wget https://raw.githubusercontent.com/rabbitmq/support-tools/main/scripts/rabbitmq-collect-env
chmod +x rabbitmq-collect-env
./rabbitmq-collect-env




rabbitmq-server/8c3624c9-f553-4d9a-ad5d-2241b3c6ed4b:~# ./rabbitmq-collect-env
[WARN] expected to find rabbitmq-env at '/usr/lib/rabbitmq/bin/rabbitmq-env', but file does not exist.
............._...........___..__.....__...................................
[INFO] output archive: '/var/vcap/sys/log/rabbitmq-server/rabbitmq-env-01d47563-3148-4c89-9ce6-0da56fc9b8bb-20240801-003415.tgz'
rabbitmq-server/8c3624c9-f553-4d9a-ad5d-2241b3c6ed4b:~#



01d47563-3148-4c89-9ce6-0da56fc9b8bb is the hostname of this rabbitmq VM and it can be found from the overview file





Please upload the tgz file to this ticket



