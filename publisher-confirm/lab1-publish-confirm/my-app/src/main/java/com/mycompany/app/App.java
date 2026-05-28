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
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 1. Enable publisher confirms on the channel
            channel.confirmSelect();

            // 2. DECLARE THE QUEUE FIRST 👈 (This fixes your issue)
            // Parameters: queueName, durable, exclusive, autoDelete, arguments
            channel.queueDeclare("my_queue", false, false, false, null);

            String message = "Hello World!";
            channel.basicPublish("", "my_queue", null, message.getBytes());

            // 3. Block until the broker ACKs or NACKs the message
            if (channel.waitForConfirms(5000)) {
                System.out.println("Message delivery confirmed.");
            } else {
                System.err.println("Message delivery failed (NACK received).");
            }
        }

    }
}
