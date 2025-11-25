import pika
import time


username = 'admin'
password = 'password'
rabbitmq_credential  = pika.PlainCredentials(username,password)

time.sleep(5)  # wait for RabbitMQ

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host='rabbitmq', credentials=rabbitmq_credential)
)

channel = connection.channel()
channel.queue_declare(queue='test')

channel.basic_publish(exchange='', routing_key='test', body='Hello from pika!')
print(" [x] Sent 'Hello from pika!'")

connection.close()
