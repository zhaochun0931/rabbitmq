package com.mycompany.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class App {

    private final static String QUEUE_NAME = "myqueue";

    public static void main(String[] argv) {
        // 1. Setup the Connection Factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");

        // 2. Try-with-resources to ensure connection and channel are closed automatically
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 3. Declare the queue as a Quorum Queue with x-max-length = 10
            Map<String, Object> args = new HashMap<>();

            // --- QUORUM QUEUE REQUIREMENT ---
            args.put("x-queue-type", "quorum");
            // --------------------------------

            args.put("x-max-length", 10);

            // queueDeclare(queue, durable, exclusive, autoDelete, arguments)
            // --- QUORUM QUEUE REQUIREMENT: MUST BE DURABLE (2nd param = true) ---
            channel.queueDeclare(QUEUE_NAME, true, false, false, args);
            System.out.println("Quorum Queue '" + QUEUE_NAME + "' declared with max length 10.");

            // 4. Send 3,000 messages to the queue
            for (int i = 1; i <= 3000; i++) {
                String message = "Message number " + i;

                // basicPublish(exchange, routingKey, basicProperties, body)
                // Using the default exchange ("") and routing directly to the queue name
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));

                // Optional: Print every 500th message to track progress in the console
                if (i % 500 == 0) {
                    System.out.println("Sent: '" + message + "'");
                }
            }

            System.out.println("Successfully sent 3000 messages to Quorum Queue '" + QUEUE_NAME + "'.");

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}