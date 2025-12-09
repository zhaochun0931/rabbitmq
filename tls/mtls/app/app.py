import pika
import ssl
import time
from datetime import datetime

# RabbitMQ connection info
RABBIT_HOST = "node2"
RABBIT_PORT = 5671
QUEUE_NAME = "test"
USERNAME = "admin"
PASSWORD = "password"



# Paths to SSL certificates and key
client_cert = '/certs/client.crt'
client_key = '/certs/client.key'
ca_cert = '/certs/ca.crt'

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
rabbitmq_credentials = pika.PlainCredentials(USERNAME, PASSWORD)
connection_parameters = pika.ConnectionParameters(
    host=RABBIT_HOST,
    port=RABBIT_PORT,
    virtual_host='/',
    ssl_options=pika.SSLOptions(context=ssl_context),
    credentials=rabbitmq_credentials
    # For EXTERNAL authentication, do not set explicit credentials
)



# Retry loop until RabbitMQ is ready
while True:
    try:
        connection = pika.BlockingConnection(connection_parameters)
        print(f"[✓] Connected to RabbitMQ at {RABBIT_HOST}:{RABBIT_PORT}")
        break
    except Exception as e:
        print(f"[!] RabbitMQ not ready yet, retrying in 2s: {e}")
        time.sleep(2)

channel = connection.channel()
channel.queue_declare(queue=QUEUE_NAME, durable=True)

# Send a test message
msg = f"Hello from pika client! {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"
channel.basic_publish(exchange="", routing_key=QUEUE_NAME, body=msg)
print(f"[✓] Sent message to queue '{QUEUE_NAME}': {msg}")


