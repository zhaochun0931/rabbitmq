import pika
import ssl
from datetime import datetime

# Define the RabbitMQ server details
the_rabbitmq_host_name = 'node1'
the_rabbitmq_queue_name = 'your_queue_name'
username = 'admin'
password = 'password'

# Paths to SSL certificates and key
client_cert = '/root/certs/myserver.crt'
client_key = '/root/certs/myserver.key'
ca_cert = '/root/certs/myca.crt'

# Get the current date and time
current_date = datetime.now()

# SSL context configuration
ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
ssl_context.load_verify_locations(ca_cert)
ssl_context.load_cert_chain(
    certfile=client_cert,
    keyfile=client_key
)

# Connection parameters with SSL options
rabbitmq_credentials = pika.PlainCredentials(username, password)
connection_parameters = pika.ConnectionParameters(
    host=the_rabbitmq_host_name,
    port=5671,
    virtual_host='/',
    ssl_options=pika.SSLOptions(context=ssl_context),
    credentials=rabbitmq_credentials
    # For EXTERNAL authentication, do not set explicit credentials
)

try:
    # Connect to RabbitMQ
    with pika.BlockingConnection(connection_parameters) as connection:
        channel = connection.channel()

        # Declare a queue
        queue_name = the_rabbitmq_queue_name
        channel.queue_declare(queue=queue_name,durable=True,arguments={"x-queue-type": "quorum"})

        # Send a message
        # message = "Hello, RabbitMQ with SSL and Client Certificate!"
        message = f"Hello, RabbitMQ with SSL and Client Certificate! {current_date.strftime('%Y-%m-%d %H:%M:%S')}"

        channel.basic_publish(exchange='', routing_key=queue_name, body=message)

        print(f"Message sent to queue '{queue_name}': {message}")
except Exception as e:
    print(f"Error connecting to RabbitMQ: {e}")
