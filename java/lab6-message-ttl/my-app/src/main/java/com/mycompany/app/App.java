package com.mycompany.app;

import com.rabbitmq.client.*;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class App {

    private final static String QUEUE_NAME = "myQueue";
    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("password");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Step 1: Define Queue TTL (60 seconds)
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", 60000); 
            
            // Note: If 'myQueue' already exists without TTL, this will throw an error.
            // You may need to call: channel.queueDelete(QUEUE_NAME);
            channel.queueDeclare(QUEUE_NAME, false, false, false, args);

            // Step 2: Set Message Expiration (30 seconds)
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .expiration("30000") // 30 seconds
                    .build();

            // Step 3: Publish with Timestamp
            String message = "Hello, RabbitMQ! I will disappear in 30s.";
            channel.basicPublish("", QUEUE_NAME, props, message.getBytes("UTF-8"));

            System.out.println("[" + LocalTime.now().format(dtf) + "] [x] Sent '" + message + "'");
            System.out.println("Check your RabbitMQ management UI. It should vanish in 30 seconds.");
        }
    }
}
