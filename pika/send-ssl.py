import pika
import ssl

# SSL context configuration
ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
ssl_context.load_verify_locations("path/to/ca_certificate.pem")
ssl_context.load_cert_chain(
    certfile="path/to/client_certificate.pem",
    keyfile="path/to/client_key.pem"
)

# Connection parameters with SSL options
connection_parameters = pika.ConnectionParameters(
    host='your.rabbitmq.host',
    port=5671,
    virtual_host='/',
    ssl_options=pika.SSLOptions(context=ssl_context),
    # For EXTERNAL authentication, do not set explicit credentials
)

try:
    # Connect to RabbitMQ
    with pika.BlockingConnection(connection_parameters) as connection:
        channel = connection.channel()

        # Declare a queue
        queue_name = 'test_ssl_queue'
        channel.queue_declare(queue=queue_name,durable=True,arguments={"x-queue-type": "quorum"})

        # Send a message
        message = "Hello, RabbitMQ with SSL and Client Certificate!"
        channel.basic_publish(exchange='', routing_key=queue_name, body=message)

        print(f"Message sent to queue '{queue_name}': {message}")
except Exception as e:
    print(f"Error connecting to RabbitMQ: {e}")
