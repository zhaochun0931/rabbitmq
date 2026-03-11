package com.mycompany.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

/**
 * A clean RabbitMQ consumer using the 'Push' model.
 * This version uses auto-acknowledgment (No manual Ack required).
 */
public class App {

    // The name of the queue we are listening to
    private final static String QUEUE_NAME = "test-queue";

    public static void main(String[] argv) throws Exception {
        
        // 1. Setup the Connection Factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("password");

        // 2. Establish Connection and Channel
        // Note: We don't use try-with-resources for the connection here 
        // because this is a long-running background listener.
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 3. Ensure the queue exists
        // durable: true, exclusive: false, autoDelete: false
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 4. Define the Callback logic
        // This code executes every time a message is pushed to us from the server.
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received: '" + message + "'");
            
            // Because autoAck is 'true' (see below), RabbitMQ will 
            // automatically delete the message from the queue NOW.
        };

        // 5. Start Consuming
        // autoAck: true (This is the 'without ack' requirement)
        boolean autoAck = true; 
        channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });
    }
}
