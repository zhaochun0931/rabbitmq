import paho.mqtt.client as mqtt

# Define the MQTT broker settings
broker_host = "localhost"  # RabbitMQ server (MQTT plugin enabled)
broker_port = 1883         # Default MQTT port for RabbitMQ
topic = "home/livingroom/temperature"  # Topic to subscribe to

# Define the username and password for RabbitMQ MQTT authentication
username = "admin"  # Replace with your RabbitMQ username
password = "password"  # Replace with your RabbitMQ password

# MQTT callback function for when the client connects to the broker
def on_connect(client, userdata, flags, rc, properties):
    print(f"Connected to broker with result code {rc}")
    # Subscribe to the topic once connected
    client.subscribe(topic)
    print(f"Subscribed to topic '{topic}'")

# MQTT callback function for when a message is received from the broker
def on_message(client, userdata, msg):
    # The msg.payload contains the message, which is in bytes
    message = msg.payload.decode("utf-8")  # Decode it to string
    print(f"Received message '{message}' from topic '{msg.topic}'")

# Create the MQTT client instance
client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)  # No need to specify API version

# Set username and password for authentication
client.username_pw_set(username, password)

# Attach the callback functions
client.on_connect = on_connect
client.on_message = on_message

# Connect to the RabbitMQ MQTT broker with the specified credentials
try:
    client.connect(broker_host, broker_port, 60)
    # Start the loop to listen for incoming messages
    client.loop_start()
except Exception as e:
    print(f"Error connecting to broker: {e}")

# Keep the script running to consume messages
try:
    while True:
        # Keep the consumer running, this can be replaced with other tasks
        pass
except KeyboardInterrupt:
    print("Exiting...")
