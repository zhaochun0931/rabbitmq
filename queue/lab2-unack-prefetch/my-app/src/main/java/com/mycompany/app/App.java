package com.mycompany.app;

/**
 * Hello world!
 */

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class App {

    private final static String QUEUE_NAME = "myqueue";

    public static void main(String[] argv) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");

        try {
            // Note: We don't use try-with-resources here because we want the
            // connection to stay open so it can listen for messages continuously.
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            // Define what happens when a message is received
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");

                // INTENTIONAL UNACK:
                // We are intentionally NOT calling channel.basicAck() here.
                // Because autoAck is false, RabbitMQ will wait for an ack that never comes,
                // keeping the message in the "Unacked" state in the queue.

                // Normal processing would include:
                // channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };


            // Set the prefetch count to a different number (e.g., 50)
            int prefetchCount = 100;

            // basicQos(prefetchSize, prefetchCount, global)
            // prefetchSize is usually 0 (unlimited payload size)
            // global=false applies the limit to the consumer level
            channel.basicQos(0, prefetchCount, false);

            // Set autoAck to false to require manual acknowledgements
            boolean autoAck = false;

            // Start consuming
            channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}