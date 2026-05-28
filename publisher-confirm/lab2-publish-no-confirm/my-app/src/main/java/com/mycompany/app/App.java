package com.mycompany.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("172.16.204.139");
//        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 1. CONFIRMS DISABLED
            // channel.confirmSelect() has been removed so the broker will not send ACKs.

            // 2. Declare the queue as durable
            channel.queueDeclare("my_queue", true, false, false, null);

            String baseMessage = "Hello World!";

            System.out.println("Sending 10 messages blindly...");

            // 3. Loop exactly 10 times
            for (int i = 1; i <= 10; i++) {
                String message = baseMessage + " - Msg #" + i;

                // Fire-and-forget publish
                channel.basicPublish("", "my_queue", null, message.getBytes());
                System.out.println("Sent: " + message);

                // 4. WAIT LOGIC REMOVED
                // No blocking or pausing to look for network responses.
            }

            System.out.println("\nAll 10 messages sent continuously.");
        }
    }
}