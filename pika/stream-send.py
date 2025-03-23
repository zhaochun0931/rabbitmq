import pika

# Connect to RabbitMQ server
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()

# Declare the Stream Queue (as before)
channel.queue_declare(queue='my_stream_queue', arguments={'x-queue-type': 'stream'})

# Publish messages to the stream
for i in range(5):
    message = f"Stream message {i}"
    channel.basic_publish(exchange='', routing_key='my_stream_queue', body=message)
    print(f"Sent: {message}")

# Closing the connection
connection.close()
