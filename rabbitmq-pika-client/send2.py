import pika

credentials = pika.PlainCredentials(username='admin', password='password')

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host='1.1.1.1', credentials=credentials))

channel = connection.channel()

channel.queue_declare(queue='Hello')

channel.basic_publish(exchange='', routing_key='Hello', body='Hey!')
print(" [x] Sent 'Hello World!' ")
connection.close()
