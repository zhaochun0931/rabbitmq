#!/usr/bin/env python
import pika
import time

username = 'admin'
password = 'password'
rabbitmq_credential  = pika.PlainCredentials(username,password)



connection = pika.BlockingConnection(pika.ConnectionParameters(
    host='localhost',
    port=5672,
    virtual_host='/',
    credentials=rabbitmq_credential
))

#connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))


channel = connection.channel()

channel.queue_declare(queue='hello')
#channel.queue_declare(queue='qq1', durable=True,arguments={"x-queue-type": "quorum"})


channel.basic_publish(exchange='', routing_key='hello', body='Hello World!')
print(" [x] Sent 'Hello World!'")


# Sleep for 10 seconds
time.sleep(10)


connection.close()
