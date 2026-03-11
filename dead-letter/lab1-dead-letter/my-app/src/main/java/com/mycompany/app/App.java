package com.mycompany.app;

import com.rabbitmq.client.*;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class App {

    private final static String MAIN_QUEUE = "main_orders_queue";
    private final static String DLX_EXCHANGE = "dlx_exchange";
    private final static String DLQ_QUEUE = "dead_letter_archive";
    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 1. Setup the Dead Letter infrastructure first
            // We create a Direct exchange and a queue to hold the "trash"
            channel.exchangeDeclare(DLX_EXCHANGE, BuiltinExchangeType.DIRECT, true);
            channel.queueDeclare(DLQ_QUEUE, true, false, false, null);
            // Bind the DLQ to the DLX (using the same routing key as the original)
            channel.queueBind(DLQ_QUEUE, DLX_EXCHANGE, MAIN_QUEUE);

            // 2. Setup the Main Queue with Dead Letter routing
            Map<String, Object> args = new HashMap<>();
            args.put("x-dead-letter-exchange", DLX_EXCHANGE);
            // Optional: You can change the routing key when it's dead-lettered
            args.put("x-dead-letter-routing-key", MAIN_QUEUE);
            // Set a short TTL for testing
            args.put("x-message-ttl", 10000); 

            // IMPORTANT: If MAIN_QUEUE exists without these args, you must delete it first!
            // channel.queueDelete(MAIN_QUEUE); 
            channel.queueDeclare(MAIN_QUEUE, true, false, false, args);

            // 3. Publish a message that will die in 10 seconds
            String message = "Sensitive Order Data (Expires in 10s)";
            channel.basicPublish("", MAIN_QUEUE, null, message.getBytes("UTF-8"));

            System.out.println("[" + LocalTime.now().format(dtf) + "] [x] Sent message to " + MAIN_QUEUE);
            System.out.println("Wait 10 seconds... RabbitMQ will move it to " + DLQ_QUEUE);
        }
    }
}
