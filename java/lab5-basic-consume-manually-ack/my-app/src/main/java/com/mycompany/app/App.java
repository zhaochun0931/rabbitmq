package com.mycompany.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class App {

    private final static String QUEUE_NAME = "test-queue";
    // Formatter to show HH:mm:ss
    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("password");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        // Ensures the server doesn't flood the consumer while it's sleeping
        channel.basicQos(1);

        System.out.println("[" + LocalTime.now().format(dtf) + "] [*] Waiting for messages...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            long deliveryTag = delivery.getEnvelope().getDeliveryTag();

            // Log the time the message was picked up
            System.out.println("[" + LocalTime.now().format(dtf) + "] [x] Received: '" + message + "'");
            
            try {
                // Simulate long-running task
                Thread.sleep(10000); 

                // Manual Acknowledgement
                channel.basicAck(deliveryTag, false);
                
                // Log the time the message was finally acknowledged
                System.out.println("[" + LocalTime.now().format(dtf) + "] [√] Done! Acknowledged tag: " + deliveryTag);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println(" [!] Error. Requeuing...");
                channel.basicNack(deliveryTag, false, true);
            }
        };

        // autoAck: false is required for manual ack
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    }
}
