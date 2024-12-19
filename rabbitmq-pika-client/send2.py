import pika

credentials = pika.PlainCredentials(username='admin', password='password')

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host='localhost', credentials=credentials))

channel = connection.channel()

channel.queue_declare(queue='Hello')
channel.queue_declare(queue='Hello-qq', durable=True,arguments={"x-queue-type": "quorum"})


channel.basic_publish(exchange='', routing_key='Hello', body='Hey!')
print(" [x] Sent 'Hello World!' ")
connection.close()
