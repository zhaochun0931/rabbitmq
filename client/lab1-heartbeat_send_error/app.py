import time
import os
import pika

RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'rabbitmq')

credentials = pika.PlainCredentials('admin', 'password')
parameters = pika.ConnectionParameters(
    host=RABBITMQ_HOST,
    port=5672,
    credentials=credentials,
    heartbeat=5  # 设置 5 秒心跳以便快速复现
)

print(f"正在尝试连接到 RabbitMQ ({RABBITMQ_HOST}:5672)...")

# 增加重试逻辑，等待 RabbitMQ 服务完全启动
connection = None
for i in range(15):
    try:
        connection = pika.BlockingConnection(parameters)
        print("连接建立成功！")
        break
    except pika.exceptions.AMQPConnectionError:
        print(f"RabbitMQ 尚未准备就绪，等待 2 秒后重试 ({i+1}/15)...")
        time.sleep(2)

if not connection:
    print("多次重试后仍无法连接到 RabbitMQ，退出。")
    exit(1)

# 1. 稍等 2 秒，让心跳机制正常启动
time.sleep(2)

print("正在通过 iptables 静默切断客户端出口流量（模拟网络静默断开）...")

# 2. 静默丢弃发往 RabbitMQ 的所有 TCP 包
os.system("iptables -A OUTPUT -p tcp --dport 5672 -j DROP")

print("网络已静默切断！请观察 RabbitMQ 容器的日志...")
print("等待 5~10 秒后，RabbitMQ 服务端在尝试发送心跳时将刷出 {heartbeat_send_error,closed} 报错。")

# 3. 保持容器运行，等待服务端触发心跳
time.sleep(15)
