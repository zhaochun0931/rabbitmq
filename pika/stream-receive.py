import pika

def callback(ch, method, properties, body):
    """
    This function will be called whenever a message is received.
    """
    print(f"Received: {body.decode()}")

def main():
    # Connect to RabbitMQ server
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    # Declare the stream queue (same declaration as when sending)
    channel.queue_declare(queue='my_stream_queue',
                          arguments={'x-queue-type': 'stream'},
                          durable=True)

    # Set the prefetch count (limit the number of unacknowledged messages)
    # This is required for stream queues to prevent the "PRECONDITION_FAILED" error
    channel.basic_qos(prefetch_count=100)  # You can adjust this number

    # Set up the consumer to consume from the stream queue
    channel.basic_consume(queue='my_stream_queue', on_message_callback=callback, auto_ack=False)

    # Print out a message to show that the consumer is waiting for messages
    print('Waiting for messages. To exit press CTRL+C')

    # Start consuming messages
    channel.start_consuming()

if __name__ == '__main__':
    main()
