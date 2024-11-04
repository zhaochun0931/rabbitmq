import paho.mqtt.client as mqtt

# MQTT broker details
BROKER = 'localhost'  # Change to your RabbitMQ server address
PORT = 1883           # Default MQTT port
TOPIC = 'weather/#'   # Subscribe to all weather-related topics

def on_connect(client, userdata, flags, rc):
    print(f"Connected with result code {rc}")
    # Subscribe to the topic when connected
    client.subscribe(TOPIC)

def on_message(client, userdata, msg):
    print(f"Received message '{msg.payload.decode()}' on topic '{msg.topic}'")

# Create an MQTT client instance
client = mqtt.Client()

# Assign the callback functions
client.on_connect = on_connect
client.on_message = on_message

# Connect to the broker
client.connect(BROKER, PORT, keepalive=60)

# Start the loop to process callbacks
client.loop_start()

try:
    # Keep the script running to receive messages
    client.loop_forever()
except KeyboardInterrupt:
    print("Disconnecting...")
finally:
    client.loop_stop()
    client.disconnect()
