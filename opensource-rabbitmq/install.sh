# The RabbitMQ server gives you a default username and password, that is guest:guest. Note that guest:guest won't work for remote a RabbitMQ server later than version 3.3
# add user and assign the permssion 

rabbitmqctl add_user admin password
rabbitmqctl set_user_tags admin administrator



# grant the user permission to modify, write, and read all vhosts
rabbitmqctl set_permissions --vhost / admin ".*" ".*" ".*"
