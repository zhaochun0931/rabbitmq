import pika
import time
from datetime import datetime


username = 'admin'
password = 'password'
rabbitmq_password  = pika.PlainCredentials(username,password)



# conn =  pika.BlockingConnection(pika.ConnectionParameters('localhost'))
# conn =  pika.BlockingConnection(pika.ConnectionParameters('localhost',credentials=rabbitmq_password))
conn =  pika.BlockingConnection(pika.ConnectionParameters('localhost','5672','/',credentials=rabbitmq_password))


chan = conn.channel()

# chan.queue_declare(queue='qq1',durable=True)
chan.queue_declare(queue='qq1',durable=True,arguments={"x-queue-type": "quorum"})






for i in range(10000):
    now = datetime.now()

    # dt_string = now.strftime("%d/%m/%Y %H:%M:%S")
    dt_string = str(i)


    chan.basic_publish(exchange='',
                       routing_key='qq1',
                       body='hello world 1111' + dt_string,mandatory=True)
    chan.confirm_delivery()



    print("message sent!")


conn.close()

