# Use Python slim image
FROM python:3.11-slim

# Set working directory inside container
WORKDIR /app

# Copy Raft node code and run script
COPY raft_node.py run.sh ./

# Make run.sh executable
RUN chmod +x run.sh

# Run the script
CMD ["./run.sh"]

