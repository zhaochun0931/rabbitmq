import pika
import time

credentials = pika.PlainCredentials('admin', 'password') # 换成你的真实密码

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
    headers = properties.headers or {}
    # 获取投递计数
    delivery_count = headers.get('x-delivery-count', 0)
    
    print(f"收到消息: {body.decode()} | x-delivery-count: {delivery_count}")
    
    time.sleep(0.5) # 稍微加一点延迟，方便看清控制台日志
    
    print("--> 发送 basic_reject(requeue=True) 触发计数递增...")
    # 注意：这里改用 basic_reject，而不是 basic_nack！
    ch.basic_reject(delivery_tag=method.delivery_tag, requeue=True)

channel.basic_consume(queue=QUEUE_NAME, on_message_callback=callback, auto_ack=False)

print(f"[*] 开始监听队列 '{QUEUE_NAME}'...")
channel.start_consuming()
