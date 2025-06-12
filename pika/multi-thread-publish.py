import threading
import pika
import time
import random

RABBITMQ_HOST = "localhost"
QUEUE_NAME = "test_queue"
NUM_THREADS = 10
MESSAGES_PER_THREAD = 10
USERNAME='your_name'
PASSWORD='your_password'

def publish_messages(thread_id):
    # Each thread establishes its own connection
    credentials = pika.PlainCredentials(USERNAME, PASSWORD)
    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=credentials))
    channel = connection.channel()

    # Ensure the queue exists (safe to call multiple times)
    channel.queue_declare(queue=QUEUE_NAME, durable=True)

    for i in range(MESSAGES_PER_THREAD):
        message = f"Message {i + 1} from Thread-{thread_id}"
        channel.basic_publish(
            exchange='',
            routing_key=QUEUE_NAME,
            body=message,
            properties=pika.BasicProperties(delivery_mode=2)  # Make message persistent
        )
        print(f"[Thread-{thread_id}] Sent: {message}")
        time.sleep(random.uniform(5, 10))  # Optional delay
        #time.sleep(10)  # Optional delay

    channel.close()
    connection.close()
    print(f"[Thread-{thread_id}] ✅ Done.")

def main():
    threads = []
    for i in range(NUM_THREADS):
        t = threading.Thread(target=publish_messages, args=(i + 1,))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    print("✅ All threads finished.")

if __name__ == "__main__":
    main()
