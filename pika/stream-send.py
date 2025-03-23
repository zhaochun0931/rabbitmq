import pika

# Connect to RabbitMQ server
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()

# Declare a Stream Queue with 'durable=True' to avoid the error
channel.queue_declare(queue='my_stream_queue',
                      arguments={'x-queue-type': 'stream'},
                      durable=True)  # Ensuring the queue is durable

print("Stream queue declared successfully.")



# Publish messages to the stream
for i in range(5):
    message = f"Stream message {i}"
    channel.basic_publish(exchange='', routing_key='my_stream_queue', body=message)
    print(f"Sent: {message}")


# Closing the connection
connection.close()
