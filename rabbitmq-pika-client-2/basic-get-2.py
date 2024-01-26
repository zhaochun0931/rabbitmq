import os
import pika
import sys

# this demo will use the basic_get to retrieve message without ACK

connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
channel = connection.channel()
channel.basic_get(queue='hello')
