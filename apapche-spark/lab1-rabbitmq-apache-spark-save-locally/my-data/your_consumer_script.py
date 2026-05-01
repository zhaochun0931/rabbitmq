import pika
import json
from pyspark.sql import SparkSession

# 1. Initialize Spark once
spark = SparkSession.builder \
    .appName("RabbitMQ_JSON_Consumer") \
    .getOrCreate()

spark.sparkContext.setLogLevel("WARN")
print("Spark Session initialized. Connecting to RabbitMQ...")

# 2. Define what happens when a message arrives
def process_message(ch, method, properties, body):
    message = body.decode('utf-8')
    print(f"\n[X] Received message: {message}")
    
    try:
        # A. Convert the JSON string into a Python dictionary
        parsed_data = json.loads(message)
        
        # B. Hand the dictionary to Spark. 
        # Spark automatically detects "user" (string) and "amount" (integer)
        df = spark.createDataFrame([parsed_data])
        df.show()
        
        # C. Save the DataFrame to disk
        # We use "append" mode so new messages add to the folder without deleting old ones
        output_path = "/opt/spark/data/transactions.parquet"
        df.write.mode("append").parquet(output_path)
        print(f"[*] Successfully saved to {output_path}")
        
        # D. Acknowledge the message
        ch.basic_ack(delivery_tag=method.delivery_tag)
        
    except json.JSONDecodeError:
        # If someone sends plain text instead of JSON, catch the error and skip it
        print("[!] Error: Message was not valid JSON. Skipping.")
        ch.basic_ack(delivery_tag=method.delivery_tag)

# 3. Set up the RabbitMQ Connection
connection = pika.BlockingConnection(pika.ConnectionParameters(host='rabbitmq'))
channel = connection.channel()

channel.queue_declare(queue='spark_trigger_queue', durable=True)
channel.basic_consume(queue='spark_trigger_queue', on_message_callback=process_message)

print(' [*] Waiting for JSON messages. To exit press CTRL+C')
channel.start_consuming()
