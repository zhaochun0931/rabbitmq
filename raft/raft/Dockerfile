FROM python:3.11-slim

WORKDIR /app

COPY raft_node.py .
COPY run.sh .
RUN chmod +x run.sh

CMD ["./run.sh"]

