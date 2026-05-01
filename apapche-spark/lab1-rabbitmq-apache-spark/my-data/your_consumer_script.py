import pika
from pyspark.sql import SparkSession

# 1. Initialize Spark once, keep it warm
spark = SparkSession.builder \
    .appName("RabbitMQ_Pika_Consumer") \
    .getOrCreate()

spark.sparkContext.setLogLevel("WARN")
print("Spark Session initialized. Connecting to RabbitMQ...")

# 2. Define what happens when a message arrives
def process_message(ch, method, properties, body):
    # Decode the binary message
    message = body.decode('utf-8')
    print(f"\n[X] Received message: {message}")
    
    # 3. Hand the data over to Spark
    # We turn the single message into a Spark DataFrame for processing
    df = spark.createDataFrame([(message,)], ["payload"])
    
    # Do your Spark transformations here
    df.show()
    
    # 4. Acknowledge the message so RabbitMQ removes it from the queue
    ch.basic_ack(delivery_tag=method.delivery_tag)

# 5. Set up the RabbitMQ Connection
# 'rabbitmq' resolves to your Docker container hostname
connection = pika.BlockingConnection(pika.ConnectionParameters(host='rabbitmq'))
channel = connection.channel()

# Ensure the queue exists
channel.queue_declare(queue='spark_trigger_queue', durable=True)

# Tell RabbitMQ to use our process_message function
channel.basic_consume(queue='spark_trigger_queue', on_message_callback=process_message)

print(' [*] Waiting for messages. To exit press CTRL+C')

# 6. Start the infinite listening loop
channel.start_consuming()
