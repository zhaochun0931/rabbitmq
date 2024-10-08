# working
import logging
import pika
import ssl
#from pika import PlainCredentials






username = 'admin'
password = 'password'
rabbitmq_password  = pika.PlainCredentials(username,password)


# logging.basicConfig(level=logging.DEBUG)

rabbitmq_context = ssl.create_default_context(cafile="/tmp/ca.crt")
rabbitmq_context.verify_mode = ssl.CERT_REQUIRED
# context.verify_mode = ssl.CERT_OPTIONAL
# context.verify_mode = ssl.CERT_NONE
rabbitmq_context.load_cert_chain("/tmp/tls.crt", "/tmp/tls.key")
rabbitmq_ssl_options = pika.SSLOptions(rabbitmq_context, "your_rabbitmq_dns_name")


rabbitmq_conn = pika.ConnectionParameters(
    host="localhost",
    port=5671,
    virtual_host='/',
    credentials=rabbitmq_password,
    ssl_options=rabbitmq_ssl_options,
     )

with pika.BlockingConnection(rabbitmq_conn) as conn:
    ch = conn.channel()
    #ch.queue_declare(queue='foobar',durable=True)
    ch.queue_declare(queue='foobar',durable=True,arguments={"x-queue-type": "quorum"})
    # chan.basic_publish(exchange='',
    #                    routing_key='qq2',
    #                    body='hello world!' + dt_string,mandatory=True)

    for i in range(100):
        ch.basic_publish("", "foobar", "Hello, world!")
    # print(ch.basic_get("foobar"))
