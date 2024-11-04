java -version

wget https://github.com/rabbitmq/rabbitmq-perf-test/releases/download/v2.21.0/perf-test-2.21.0.jar







# The most basic way of running PerfTest only specifies a URI to connect to, a number of publishers to use (say, 1) and a number of consumers to use (say, 2). 
java -jar perf-test-2.21.0.jar
java -jar perf-test-2.21.0.jar --quorum-queue --queue qq
java -jar perf-test-2.21.0.jar --stream-queue --queue sq

 -x,--producers <arg>                           producer count, default is 1

 -y,--consumers <arg>                           consumer count, default is 1
                                                









java -jar perf-test-2.21.0.jar  -x 1 -y 2 -s 4096 -h amqp://admin:password@localhost:5672


java -jar perf-test-2.21.0.jar  -x 1 -y 1 -s 4096 --quorum-queue --queue qq1  -h amqp://admin:password@localhost:5672
