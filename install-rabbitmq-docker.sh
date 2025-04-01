docker run -d -it --rm --name rabbitmq --hostname rabbitmq -p 5672:5672 -p 15672:15672 -p 1883:1883 \
-e RABBITMQ_ERLANG_COOKIE=rabbitmq \
-e RABBITMQ_DEFAULT_USER=admin \
-e RABBITMQ_DEFAULT_PASS=password \
rabbitmq:management
