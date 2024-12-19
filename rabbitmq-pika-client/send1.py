import pika
import time
from datetime import datetime


username = 'admin'
password = 'password'
rabbitmq_password  = pika.PlainCredentials(username,password)



connection =  pika.BlockingConnection(pika.ConnectionParameters('localhost','5672','/',credentials=rabbitmq_password))


channel = connection.channel()

# declare the classic queue
# channel.queue_declare(queue='hello',durable=True)

# declare the quorum queue
channel.queue_declare(queue='hello-qq',durable=True,arguments={"x-queue-type": "quorum"})






for i in range(10000):
    now = datetime.now()

    # dt_string = now.strftime("%d/%m/%Y %H:%M:%S")
    dt_string = str(i)


    channel.basic_publish(exchange='',
                       routing_key='hello-qq',
                       body='hello world ' + dt_string,mandatory=True)
    channel.confirm_delivery()



    print("message sent!")


connection.close()

