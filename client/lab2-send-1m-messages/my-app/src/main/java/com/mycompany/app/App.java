package com.mycompany.app;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.HashMap;
import java.util.Map;



/**
 * Hello world!
 */
public class App {

    private static final String QUEUE_NAME = "classic_load_test_queue";
    private static final int TOTAL_MESSAGES = 1_000_000;
    private static final int BATCH_SIZE = 5_000; // Track and confirm in batches
    
    public static void main(String[] args) {
        System.out.println("Hello World!");

        ConnectionFactory factory = new ConnectionFactory();

        // Configure connection settings
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("password");
        factory.setVirtualHost("/");

        System.out.println("Connecting to RabbitMQ...");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 1. Enable Publisher Confirms on the channel
            channel.confirmSelect();

            // 2. Assert a durable classic queue
            Map<String, Object> queueArgs = new HashMap<>();
            queueArgs.put("x-queue-type", "classic");
            channel.queueDeclare(QUEUE_NAME, true, false, false, queueArgs);

            System.out.printf("Starting load test: Sending %,d messages...%n", TOTAL_MESSAGES);
            long startTime = System.currentTimeMillis();

            int outstandingConfirms = 0;

            for (int i = 1; i <= TOTAL_MESSAGES; i++) {
                String message = String.format("{\"id\":%d,\"timestamp\":%d,\"metric\":\"performance_test_data_point\"}",
                        i, System.currentTimeMillis());

                // 3. Publish message (MessageProperties.PERSISTENT_TEXT_PLAIN ensures it survives broker restarts)
                channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
                outstandingConfirms++;

                // 4. Once batch size is reached, wait for RabbitMQ to ACK the batch
                if (outstandingConfirms == BATCH_SIZE) {
                    channel.waitForConfirmsOrDie(5000); // 5-second timeout safeguard
                    outstandingConfirms = 0;
                }

                // Progress log every 100,000 messages
                if (i % 100_000 == 0) {
                    double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
                    long rate = Math.round(i / elapsed);
                    System.out.printf("Progress: %,d / %,d sent. Rate: %,d msg/sec%n", i, TOTAL_MESSAGES, rate);
                }
            }

            // Clear out any remaining messages left over in the final partial batch
            if (outstandingConfirms > 0) {
                channel.waitForConfirmsOrDie(5000);
            }

            double totalTime = (System.currentTimeMillis() - startTime) / 1000.0;
            System.out.println("\n--- Success! ---");
            System.out.printf("Total Time: %.2f seconds%n", totalTime);
            System.out.printf("Average Speed: %,d msg/sec%n", Math.round(TOTAL_MESSAGES / totalTime));

        } catch (Exception e) {
            System.err.println("An error occurred during mass publishing:");
            e.printStackTrace();
        }

    }
}
