#!/usr/bin/env python
import pika
import time


connection = pika.BlockingConnection(
    pika.ConnectionParameters(host='localhost'))
channel = connection.channel()

channel.queue_declare(queue='hello')

channel.basic_publish(exchange='', routing_key='hello', body='Hello World!')
print(" [x] Sent 'Hello World!'")


# Sleep for 10 seconds
time.sleep(10)


connection.close()
