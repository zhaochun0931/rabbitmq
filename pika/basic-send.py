#!/usr/bin/env python
import pika
import time

username = 'admin'
password = 'password'
rabbitmq_password  = pika.PlainCredentials(username,password)



#connection = pika.BlockingConnection(pika.ConnectionParameters('localhost','5672','/',credentials=rabbitmq_password))

connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))

#connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost', credentials=credentials))

channel = connection.channel()

channel.queue_declare(queue='hello')

channel.basic_publish(exchange='', routing_key='hello', body='Hello World!')
print(" [x] Sent 'Hello World!'")


# Sleep for 10 seconds
time.sleep(10)


connection.close()
