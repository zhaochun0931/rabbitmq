import paho.mqtt.client as mqtt
import time

# MQTT broker details
BROKER = 'localhost'  # Change to your RabbitMQ server address
PORT = 1883           # Default MQTT port
TOPIC = 'weather/us/ny'  # Example topic for the messages

def on_connect(client, userdata, flags, rc):
    print(f"Connected with result code {rc}")

# Create an MQTT client instance
client = mqtt.Client()
client.on_connect = on_connect

# Connect to the broker
try:
    client.connect(BROKER, PORT, keepalive=60)
except Exception as e:
    print(f"Could not connect to broker: {e}")
    exit(1)

client.loop_start()

# Send 100 messages
for i in range(100):
    message = f"It's a sunny day in New York! Message number: {i + 1}"
    client.publish(TOPIC, message.encode('utf-8'))  # Ensure proper encoding
    print(f"Sent: {message}")
    time.sleep(1)

client.loop_stop()
client.disconnect()
