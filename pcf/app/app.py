import os
import json
import pika
from flask import Flask

app = Flask(__name__)


# ---------------------------------------------------------
# Load RabbitMQ AMQP URL from PCF VCAP_SERVICES
# ---------------------------------------------------------
def get_rabbitmq_url():
    vcap_services = os.environ.get("VCAP_SERVICES")
    if not vcap_services:
        print("⚠ No VCAP_SERVICES found, running locally?")
        return "amqp://guest:guest@localhost:5672/"

    services = json.loads(vcap_services)

    # PCF RabbitMQ service key is usually "p.rabbitmq"
    rabbitmq_service = services.get("p.rabbitmq")
    if not rabbitmq_service:
        raise Exception("RabbitMQ service not found in VCAP_SERVICES")

    creds = rabbitmq_service[0]["credentials"]

    # If URI already provided, use it directly (recommended)
    if "uri" in creds:
        return creds["uri"]

    # Otherwise manually construct AMQP URL
    return f"amqps://{creds['username']}:{creds['password']}@" \
           f"{creds['hostname']}:{creds['port']}/{creds['vhost']}"


# ---------------------------------------------------------
# Create RabbitMQ connection on startup
# ---------------------------------------------------------
rabbitmq_url = get_rabbitmq_url()
params = pika.URLParameters(rabbitmq_url)
connection = pika.BlockingConnection(params)
channel = connection.channel()

# Ensure queue exists
channel.queue_declare(queue="demo_queue", durable=True)


# ---------------------------------------------------------
# Flask routes
# ---------------------------------------------------------
@app.route("/")
def index():
    return "Python RabbitMQ Demo Running!"


@app.route("/send/<msg>")
def send(msg):
    channel.basic_publish(
        exchange='',
        routing_key='demo_queue',
        body=msg
    )
    return f"Sent message: {msg}"


@app.route("/receive")
def receive():
    method_frame, header_frame, body = channel.basic_get("demo_queue", auto_ack=True)

    if body:
        return f"Received: {body.decode()}"
    else:
        return "Queue empty."


# ---------------------------------------------------------
# Start Flask app
# ---------------------------------------------------------
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8080))
    app.run(host="0.0.0.0", port=port)
