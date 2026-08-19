FROM python:3.10-slim

# 1. 替换 Apt 源并安装 iptables 工具
RUN apt-get update && apt-get install -y iptables && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 2. 使用清华源安装 pika（增加超时时间并信任镜像源域名）
RUN pip install --no-cache-dir \
    -i https://pypi.tuna.tsinghua.edu.cn/simple \
    --trusted-host pypi.tuna.tsinghua.edu.cn \
    --default-timeout=100 \
    pika

COPY app.py .

CMD ["python", "-u", "app.py"]
