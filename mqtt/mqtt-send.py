import paho.mqtt.client as mqtt

# Define the MQTT broker settings
broker_host = "localhost"  # RabbitMQ server (MQTT plugin enabled)
broker_port = 1883         # Default MQTT port for RabbitMQ
topic = "home/livingroom/temperature"  # Topic to publish to

# Define the username and password for RabbitMQ MQTT authentication
username = "admin"  # Replace with your RabbitMQ username
password = "password"  # Replace with your RabbitMQ password

# MQTT callback function for when the client connects to the broker
def on_connect(client, userdata, flags, rc):
    print(f"Connected with result code {rc}")
    # Once connected, we can start publishing messages
    client.publish(topic, payload="22.5", qos=0, retain=True)
    print(f"Message sent to topic '{topic}': 22.5")

# MQTT callback function for when a message is published
def on_publish(client, userdata, mid):
    print(f"Message {mid} has been successfully published!")

# Create the MQTT client instance
client = mqtt.Client()

# Set username and password for authentication
client.username_pw_set(username, password)

# Attach the callback functions
client.on_connect = on_connect
client.on_publish = on_publish

# Connect to the RabbitMQ MQTT broker with the specified credentials
try:
    client.connect(broker_host, broker_port, 60)
    # Start the MQTT client loop to handle the communication
    client.loop_forever()
except Exception as e:
    print(f"Error connecting to broker: {e}")

