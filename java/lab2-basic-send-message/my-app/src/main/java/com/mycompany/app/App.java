package com.mycompany.app;

import com.rabbitmq.client.*;

/**
 * App class that demonstrates a simple Producer pattern.
 * This script connects to a RabbitMQ broker and publishes 100 messages 
 * to a Direct Exchange.
 */
public class App {

    // Define constants for the messaging topology
    private final static String EXCHANGE_NAME = "my_exchange";
    private final static String QUEUE_NAME = "my_queue";
    private final static String ROUTING_KEY = "my_routing_key";

    public static void main(String[] args) {
        System.out.println("Initializing RabbitMQ Producer...");

        // --- SECTION 1: CONFIGURATION ---
        // ConnectionFactory is used to configure the connection settings to the RabbitMQ server.
        ConnectionFactory factory = new ConnectionFactory();
        
        factory.setHost("localhost");      // The IP or hostname where RabbitMQ is running
        factory.setPort(5672);             // Standard AMQP port (use 5671 for TLS/SSL)
        factory.setUsername("admin");      // Credentials configured in RabbitMQ
        factory.setPassword("password");   // Credentials configured in RabbitMQ
        factory.setVirtualHost("/");       // Logical grouping within RabbitMQ (default is "/")

        // --- SECTION 2: CONNECTION & CHANNEL ---
        // 'try-with-resources' ensures that the Connection and Channel are closed
        // automatically even if an exception occurs during execution.
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            /* * Declare the Exchange:
             * exchange: name of the exchange
             * type: DIRECT sends messages to queues based on exact routing key match
             * durable: true means the exchange survives a server restart
             */
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

            /* * Declare the Queue:
             * queue: name of the queue
             * durable: true (survives restart)
             * exclusive: false (can be accessed by other connections)
             * autoDelete: false (queue remains even if no consumers are connected)
             */
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            /* * Bind the Queue to the Exchange:
             * This tells the exchange to route any message with 'my_routing_key' into 'my_queue'.
             */
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

            // --- SECTION 3: PUBLISHING LOOP ---
            System.out.println("Starting message broadcast...");

            for (int i = 1; i <= 100; i++) {
                // Construct the payload for each message
                String message = "Payload ID: " + i + " - Hello RabbitMQ!";
                
                /*
                 * basicPublish:
                 * 1. exchange: The target exchange
                 * 2. routingKey: The key used by the exchange to decide where to send the message
                 * 3. props: We use PERSISTENT_TEXT_PLAIN so messages are written to disk
                 * 4. body: The message content converted to a byte array (UTF-8)
                 */
                channel.basicPublish(
                    EXCHANGE_NAME, 
                    ROUTING_KEY, 
                    MessageProperties.PERSISTENT_TEXT_PLAIN, 
                    message.getBytes("UTF-8")
                );

                // Log progress to the console
                System.out.println(" [x] Sent '" + message + "'");
            }

            System.out.println("\nSuccessfully dispatched 100 messages to the broker.");

        } catch (java.io.IOException | java.util.concurrent.TimeoutException e) {
            // Handle network errors or connection timeouts
            System.err.println("CRITICAL: Could not connect to RabbitMQ. Ensure the service is running.");
            e.printStackTrace();
        } catch (Exception e) {
            // Catch-all for any other logic errors
            System.err.println("An unexpected error occurred:");
            e.printStackTrace();
        }
    }
}
