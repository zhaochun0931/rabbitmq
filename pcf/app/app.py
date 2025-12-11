import os
import json
import pika
from flask import Flask

app = Flask(__name__)

# ---------------------------------------------------------
# Parse RabbitMQ URL from PCF
# ---------------------------------------------------------
def get_rabbitmq_url():
    vcap_services = os.environ.get("VCAP_SERVICES")

    if not vcap_services:
        print("Running locally without VCAP")
        return "amqp://guest:guest@localhost:5672/"

    services = json.loads(vcap_services)
    rabbitmq_service = services.get("p.rabbitmq")

    if not rabbitmq_service:
        raise Exception("RabbitMQ service not found in VCAP_SERVICES")

    creds = rabbitmq_service[0]["credentials"]

    # Prefer built-in URI
    if "uri" in creds:
        return creds["uri"]

    # Backup: new PCF format
    if "protocols" in creds and "amqp" in creds["protocols"]:
        return creds["protocols"]["amqp"]["uri"]

    # Fallback
    return f"amqps://{creds['username']}:{creds['password']}@" \
           f"{creds['hostname']}:{creds['port']}/{creds['vhost']}"


# ---------------------------------------------------------
# Create a NEW connection per request (PCF safe)
# ---------------------------------------------------------
def get_channel():
    url = get_rabbitmq_url()
    params = pika.URLParameters(url)
    connection = pika.BlockingConnection(params)
    channel = connection.channel()
    channel.queue_declare(queue="demo_queue", durable=True)
    return connection, channel


# ---------------------------------------------------------
# Web routes
# ---------------------------------------------------------
@app.route("/")
def index():
    return "Python RabbitMQ Demo Running"


@app.route("/send/<msg>")
def send(msg):
    connection, channel = get_channel()
    channel.basic_publish(exchange="", routing_key="demo_queue", body=msg)
    connection.close()
    return f"Sent: {msg}"


@app.route("/receive")
def receive():
    connection, channel = get_channel()
    method, props, body = channel.basic_get("demo_queue", auto_ack=True)

    connection.close()

    if body:
        return f"Received: {body.decode()}"
    else:
        return "Queue empty"


# ---------------------------------------------------------
# Run flask
# ---------------------------------------------------------
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8080))
    app.run(host="0.0.0.0", port=port)
