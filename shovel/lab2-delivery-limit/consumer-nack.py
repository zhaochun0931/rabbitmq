import pika
import time

credentials = pika.PlainCredentials('admin', 'password') # 换成你的密码

connection = pika.BlockingConnection(
    pika.ConnectionParameters(
        host='localhost',
        port=5672,
        credentials=credentials
    )
)

channel = connection.channel()
QUEUE_NAME = 'qq1'

def callback(ch, method, properties, body):
    # 获取真正的 delivery-count 计数
    headers = properties.headers or {}
    delivery_count = headers.get('x-delivery-count', 0)
    
    print(f"收到消息: {body.decode()} | 真实投递次数 (x-delivery-count): {delivery_count}")
    
    # 稍微 sleep 0.5~1 秒，给 RabbitMQ 状态更新的时间
    time.sleep(1)
    
    print("--> 拒收消息并要求重新入队 Nack(requeue=True)...")
    ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)

channel.basic_consume(queue=QUEUE_NAME, on_message_callback=callback, auto_ack=False)

print(f"[*] 开始监听队列 '{QUEUE_NAME}'，请观察 x-delivery-count 的变化...")
channel.start_consuming()
