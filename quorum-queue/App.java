package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class App {
    private static final String QUEUE_NAME = "my_quorum_queue";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("node1");
        factory.setUsername("admin");
        factory.setPassword("password");

        try (Connection conn = factory.newConnection(); Channel channel = conn.createChannel()) {
            // Declare quorum queue
            Map<String, Object> quorumArgs = new HashMap<>();
            quorumArgs.put("x-queue-type", "quorum");

            channel.queueDeclare(QUEUE_NAME, true, false, false, quorumArgs);
            System.out.println("✔ Quorum queue created");

            // Publish some messages
            for (int i = 1; i <= 100000; i++) {
                String msg = "Message-" + i;
                channel.basicPublish("", QUEUE_NAME, null, msg.getBytes(StandardCharsets.UTF_8));
                System.out.println("Published: " + msg);
            }
        }
    }
}
