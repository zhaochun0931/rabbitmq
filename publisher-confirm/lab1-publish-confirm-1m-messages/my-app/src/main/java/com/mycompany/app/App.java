package com.mycompany.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting high-throughput publisher...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");

        int totalMessages = 1_000_000;
        int batchSize = 5_000; // Outstanding messages to allow before waiting for ACKs

        long startTime = System.currentTimeMillis();

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 1. Enable publisher confirms on the channel
            channel.confirmSelect();

            // 2. Declare the queue
            channel.queueDeclare("my_queue", false, false, false, null);

            String message = "Hello World!";
            byte[] messageBytes = message.getBytes();

            for (int i = 1; i <= totalMessages; i++) {
                channel.basicPublish("", "my_queue", null, messageBytes);

                // 3. Wait for confirms in batches rather than for every single message
                if (i % batchSize == 0) {
                    System.out.println("Sent " + i + " messages. Waiting for batch ACKs...");

                    // Blocks until all outstanding messages on this channel have been ACKed
                    boolean allAcked = channel.waitForConfirms(10_000);

                    if (!allAcked) {
                        System.err.println("Some messages in the batch were NACKed or lost!");
                        // In production, you would implement a retry/handling strategy here
                    }
                }
            }

            // 4. Catch any remaining messages if totalMessages is not perfectly divisible by batchSize
            if (totalMessages % batchSize != 0) {
                channel.waitForConfirms(10_000);
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Finished! Sent " + totalMessages + " messages in " + (endTime - startTime) + "ms");
        }
    }
}