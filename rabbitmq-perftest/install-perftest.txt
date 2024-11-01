java -version

wget https://github.com/rabbitmq/rabbitmq-perf-test/releases/download/v2.21.0/perf-test-2.21.0.jar










java -jar perf-test-2.21.0.jar  -x 1 -y 2 -s 4096 -h amqp://admin:password@localhost:5672


java -jar perf-test-2.21.0.jar  -x 1 -y 1 -s 4096 --quorum-queue --queue qq1  -h amqp://admin:password@localhost:5672
