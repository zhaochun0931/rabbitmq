import pika
import time

credentials = pika.PlainCredentials('your_username', 'your_password')

# Establish a connection to RabbitMQ with a low heartbeat interval
parameters = pika.ConnectionParameters(
    host='localhost',
    port=5672,
    heartbeat=4,  # Set heartbeat to 1 second (very short)
    virtual_host='/',
    credentials=credentials
)

# Establish connection to RabbitMQ
connection = pika.BlockingConnection(parameters)
channel = connection.channel()

# Declare a queue
channel.queue_declare(queue='test_queue', durable=True)  # Ensure the queue is declared

# Simulate the delay by not sending heartbeats by sleeping for a long time
print("Simulating a delay to trigger heartbeat timeout...")
time.sleep(30)  # Wait for more than the heartbeat interval to simulate timeout
#print("hello")

# Close the connection manually, which simulates an error due to the timeout
connection.close()
