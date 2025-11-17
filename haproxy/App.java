package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class App {

    public String getGreeting() {
        return "Hello World!";
    }

    private final static String QUEUE_NAME = "hello";
    
    public static void main(String[] argv) throws Exception {

        System.out.println(new App().getGreeting());

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");
        factory.setHost("haproxy");
        factory.setPort(8888);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel())
        {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            for (int i = 1; i <= 1000; i++) {
                String now = LocalDateTime.now().format(dtf);
                String message = "Message #" + i + " at " + now;
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent: " + message);
            }
        }

    }
}
