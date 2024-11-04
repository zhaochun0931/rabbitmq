import paho.mqtt.client as mqtt
import time

# MQTT broker details
BROKER = 'localhost'  # Change this to your RabbitMQ server address if needed
PORT = 1883           # Default MQTT port
TOPIC = 'test'  # Topic to subscribe/publish to

# Callback when the client connects to the broker
def on_connect(client, userdata, flags, rc):
    print(f"Connected with result code {rc}")
    client.subscribe(TOPIC)

# Callback when a message is received
def on_message(client, userdata, msg):
    print(f"Received message '{msg.payload.decode()}' on topic '{msg.topic}'")

# Create an MQTT client instance
client = mqtt.Client()

# Assign the callback functions
client.on_connect = on_connect
client.on_message = on_message

# Connect to the broker
try:
    client.connect(BROKER, PORT, keepalive=60)
except Exception as e:
    print(f"Could not connect to broker: {e}")
    exit(1)

# Start the loop to process callbacks
client.loop_start()

# Publish a message after a brief wait to ensure connection
time.sleep(1)
client.publish(TOPIC, "Hello, RabbitMQ with MQTT!")

try:
    # Keep the script running to receive messages
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    print("Disconnecting...")
finally:
    client.loop_stop()
    client.disconnect()
