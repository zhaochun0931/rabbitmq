import paho.mqtt.client as mqtt

# MQTT broker details
BROKER = 'localhost'  # Change to your RabbitMQ server address
PORT = 1883           # Default MQTT port
TOPIC = 'weather/us/ny'  # Example topic for the message

def on_connect(client, userdata, flags, rc):
    print(f"Connected with result code {rc}")
    # Publish a message when connected
    client.publish(TOPIC, "It's a sunny day in New York!")

# Create an MQTT client instance
client = mqtt.Client()

# Assign the callback function
client.on_connect = on_connect

# Connect to the broker
try:
    client.connect(BROKER, PORT, keepalive=60)
except Exception as e:
    print(f"Could not connect to broker: {e}")
    exit(1)

# Start the loop to process callbacks
client.loop_start()

# Keep the script running for a moment to ensure the message is sent
try:
    client.loop_forever()
except KeyboardInterrupt:
    print("Disconnecting...")
finally:
    client.loop_stop()
    client.disconnect()
