import pika
import json
from pyspark.sql import SparkSession

# 1. Initialize Spark
spark = SparkSession.builder \
    .appName("Spark_to_RabbitMQ_Producer") \
    .getOrCreate()

spark.sparkContext.setLogLevel("WARN")

# 2. Load the data you saved earlier
print("Loading data from Parquet...")
df = spark.read.parquet("/opt/spark/data/transactions.parquet")
df.show()

# 3. Define the function that runs on the WORKER nodes
def send_partition_to_rabbitmq(partition):
    # A. Open connection to RabbitMQ (this runs on the worker container)
    connection = pika.BlockingConnection(pika.ConnectionParameters(host='rabbitmq'))
    channel = connection.channel()
    
    # B. Declare a NEW output queue
    channel.queue_declare(queue='spark_output_queue', durable=True)
    
    # C. Loop through the rows in this worker's chunk of data
    for row in partition:
        # Convert the Spark Row into a Python dictionary, then to a JSON string
        message_dict = row.asDict()
        message_body = json.dumps(message_dict)
        
        # Publish to RabbitMQ
        channel.basic_publish(
            exchange='',
            routing_key='spark_output_queue',
            body=message_body,
            properties=pika.BasicProperties(
                delivery_mode=2,  # Make message persistent
            )
        )
        print(f"Sent: {message_body}")
        
    # D. Close the connection when this chunk is finished
    connection.close()

# 4. Trigger the distributed action!
print("Instructing workers to send data to RabbitMQ...")

# foreachPartition divides the DataFrame and sends it to the workers to execute the function
df.foreachPartition(send_partition_to_rabbitmq)

print("[*] All data successfully sent to RabbitMQ!")
spark.stop()
