import pika, sys, os
import time


username = 'admin'
password = 'password'
rabbitmq_password  = pika.PlainCredentials(username,password)


def main():
    # conn = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
    conn = pika.BlockingConnection(pika.ConnectionParameters('localhost','5672', credentials=rabbitmq_password))

    channel = conn.channel()

    channel.queue_declare(queue='qq1',durable=True, arguments={"x-queue-type": "quorum"})

    def callback(ch, method, properties, body):
        print(" [x] Received %r" % body)

    t = time.localtime()
    current_time = time.strftime("%H:%M:%S", t)
    print(current_time)

    channel.basic_consume(queue='qq1', on_message_callback=callback, auto_ack=True)
    # time.sleep(20)

    print(' [*] Waiting for messages. To exit press CTRL+C')
    channel.start_consuming()





if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print('Interrupted')
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
