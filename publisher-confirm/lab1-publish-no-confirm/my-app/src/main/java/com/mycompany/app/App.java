package com.mycompany.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting blind high-speed publisher (No ACKs)...");

        // 1. Configure the connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");

        int totalMessages = 1_000_000;
        String message = "Hello World!";

        // Optimize payload generation outside the loop
        byte[] messageBytes = message.getBytes();

        long startTime = System.currentTimeMillis();

        // 2. Establish connection and channel using try-with-resources
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // NOTE: We DO NOT call channel.confirmSelect() here.
            // This tells RabbitMQ NOT to track or send ACKs for our messages.

            // 3. Ensure the target queue exists
            channel.queueDeclare("my_queue", false, false, false, null);

            System.out.println("Streaming 1,000,000 messages...");

            // 4. Loop and fire messages continuously
            for (int i = 1; i <= totalMessages; i++) {
                channel.basicPublish("", "my_queue", null, messageBytes);

                // Optional: Print progress every 100k messages so you know it's working
                if (i % 100_000 == 0) {
                    System.out.println("Sent " + i + " messages...");
                }
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("\nFinished!");
            System.out.println("Sent " + totalMessages + " messages blindly in " + totalTime + "ms");
            System.out.println("Throughput: " + (totalMessages / (totalTime / 1000.0)) + " msg/sec");

        } catch (Exception e) {
            System.err.println("An error occurred during publishing:");
            e.printStackTrace();
        }
    }
}