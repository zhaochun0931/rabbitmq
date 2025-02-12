import pika
import ssl

# Define the RabbitMQ server details
rabbitmq_host = 'your.rabbitmq.server'
queue_name = 'your_queue_name'
username = 'your_username'
password = 'your_password'

# Paths to SSL certificates and key
client_cert = '/path/to/client-cert.pem'
client_key = '/path/to/client-key.pem'
ca_cert = '/path/to/ca-cert.pem'

# Set up SSL context
context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
context.load_cert_chain(certfile=client_cert, keyfile=client_key)
context.load_verify_locations(cafile=ca_cert)

# SSL context will be used in the connection parameters
ssl_options = pika.SSLOptions(context, rabbitmq_host)

# Set up the RabbitMQ connection parameters with username and password
rabbitmq_credentials = pika.PlainCredentials(username, password)
parameters = pika.ConnectionParameters(
    host=rabbitmq_host,
    port=5671,
    ssl_options=ssl_options,
    credentials=rabbitmq_credentials  # Add credentials to the connection
)

# Create a connection and a channel
connection = pika.BlockingConnection(parameters)
channel = connection.channel()

# Declare a queue
channel.queue_declare(queue=queue_name,durable=True,arguments={"x-queue-type": "quorum"})

# Define the callback for consuming messages
def callback(ch, method, properties, body):
    print(f"Received message: {body.decode()}")

# Set up the consumer
channel.basic_consume(queue=queue_name, on_message_callback=callback, auto_ack=True)

print('Waiting for messages. To exit press CTRL+C')
# Start consuming
channel.start_consuming()
