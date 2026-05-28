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
//        factory.setHost("172.16.204.139");
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 1. Enable publisher confirms on the channel
            channel.confirmSelect();

            // 2. Declare the queue as durable (matches your latest fix)
            channel.queueDeclare("my_queue", true, false, false, null);

            String baseMessage = "Hello World!";

            // 3. Loop exactly 10 times
            for (int i = 1; i <= 10; i++) {
                // Appending the loop index so you can see unique messages in the queue
                String message = baseMessage + " - Msg #" + i;

                channel.basicPublish("", "my_queue", null, message.getBytes());
                System.out.println("Sent: " + message);

                // 4. Block and wait for the ACK for this specific message
                if (channel.waitForConfirms(5000)) {
                    System.out.println("-> Message " + i + " delivery confirmed by broker.");
                } else {
                    System.err.println("-> Message " + i + " delivery failed (NACK received).");
                }
            }

            System.out.println("\nAll 10 messages processed.");
        }
    }
}