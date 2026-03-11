package com.mycompany.app;

import com.rabbitmq.client.*;

/**
 * Consumer application that retrieves messages from RabbitMQ.
 * This version uses the "Pull" model (basicGet) to fetch messages.
 */
public class App {

    // Must match the queue name used by the Producer exactly
    private final static String QUEUE_NAME = "test-queue"; 

    public static void main(String[] argv) throws Exception {
        
        // --- 1. SETTING UP THE CONNECTION ---
        // The ConnectionFactory handles the socket-level settings for the broker.
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");      // Address of the RabbitMQ server
        factory.setPort(5672);             // Default AMQP port
        factory.setUsername("admin");      // Authentication: Username
        factory.setPassword("password");   // Authentication: Password

        // --- 2. ESTABLISHING THE CHANNEL ---
        // Using try-with-resources ensures the connection closes even if an error occurs.
        // In RabbitMQ, 'Connections' are long-lived, while 'Channels' are virtual connections 
        // inside the socket used for specific API commands.
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            /* * Declare the queue:
             * This ensures the queue exists before we try to pull from it.
             * durable: true (Matches the Producer's setting to avoid PRECONDITION_FAILED)
             * exclusive: false (Allows multiple consumers to access this queue)
             * autoDelete: false (Keep the queue even if we disconnect)
             */
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            System.out.println(" [*] Checking for messages in " + QUEUE_NAME + "...");

            // --- 3. FETCHING THE MESSAGES (THE LOOP) ---
            int count = 0;
            boolean moreMessages = true;

            while (moreMessages) {
                /*
                 * basicGet: This is a synchronous "Pull" request.
                 * 1st param: The queue name.
                 * 2nd param (autoAck): true. This tells RabbitMQ to remove the message from 
                 * the queue immediately after it's handed to us.
                 */
                GetResponse response = channel.basicGet(QUEUE_NAME, true);

                if (response != null) {
                    // Extract the byte array from the response and convert to a String
                    String message = new String(response.getBody(), "UTF-8");
                    count++;
                    System.out.println(" [√] Received (" + count + "): " + message);
                } else {
                    // response is null when the queue is empty
                    System.out.println(" [!] No more messages in the queue.");
                    moreMessages = false; 
                }
            }

            System.out.println("Finished processing " + count + " messages.");
        }
    }
}
