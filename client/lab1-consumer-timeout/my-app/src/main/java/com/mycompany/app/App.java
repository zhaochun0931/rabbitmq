package com.mycompany.app;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Hello world!
 */
public class App {

    private final static String QUEUE_NAME = "timeout-test-queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("Hello World!");

        ConnectionFactory factory = new ConnectionFactory();
        // factory.setHost("your-tanzu-rabbitmq-host");
        factory.setHost("localhost");

        // Change these to your actual Tanzu RabbitMQ credentials
        factory.setUsername("admin");
        factory.setPassword("password");

        // If you are using a specific vhost (Tanzu often isolates apps into specific vhosts)
        factory.setVirtualHost("/"); // or "your-vhost-name"

        factory.setRequestedHeartbeat(60); // Keep heartbeats healthy

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 1. Declare the durable queue
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        // 2. Set prefetch to 400 (matches your application configuration)
        channel.basicQos(400);

        // 3. Populate the queue with 1,000 messages first
        System.out.println(" [^] Publishing 1,000 test messages to " + QUEUE_NAME + "...");
        String messagePayload = "Timeout test payload";
        for (int i = 0; i < 1000; i++) {
            channel.basicPublish("", QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    messagePayload.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println(" [v] Done! 1,000 messages are now sitting in the queue.");

        System.out.println(" [*] Starting consumer. Stalling thread to trigger the 30-min timeout...");

        // 4. Define the consumer callback
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            System.out.println(" [x] Received a message chunk. Sleeping worker thread for 31 minutes...");

            try {
                // Sleep for 31 minutes to deliberately breach the 30-minute consumer timeout
                Thread.sleep(1_860_000);

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            } catch (InterruptedException e) {
                System.err.println(" [!] Thread was interrupted: " + e.getMessage());
            } catch (IOException e) {
                System.err.println(" [!] Failed to ACK because RabbitMQ killed the channel: " + e.getMessage());
            }
        };

        // 5. Start consuming with manual ACKs
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    }
}