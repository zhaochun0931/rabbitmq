package com.mycompany.app;

import com.rabbitmq.client.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // --- 1. SET UP DEAD LETTER INFRASTRUCTURE ---
        channel.exchangeDeclare(DLX_EXCHANGE, BuiltinExchangeType.DIRECT, true);
        channel.queueDeclare(DLQ_QUEUE, true, false, false, null);
        channel.queueBind(DLQ_QUEUE, DLX_EXCHANGE, "reject_key");

        // --- 2. SET UP MAIN QUEUE WITH DLX LINK ---
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", "reject_key"); // Route specifically to DLQ

        // IMPORTANT: If MAIN_QUEUE exists without these args, delete it first:
        // channel.queueDelete(MAIN_QUEUE); 
        channel.queueDeclare(MAIN_QUEUE, true, false, false, args);

        System.out.println("[" + LocalTime.now().format(dtf) + "] [*] Waiting for messages to reject...");

        // --- 3. CONSUME AND REJECT ---
        channel.basicQos(1);
        
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            long deliveryTag = delivery.getEnvelope().getDeliveryTag();

            System.out.println("[" + LocalTime.now().format(dtf) + "] [x] Received: " + message);

            // Logic: Always reject for this demo
            System.out.println("[" + LocalTime.now().format(dtf) + "] [!] REJECTING: Moving to DLQ now.");

            /* * basicReject(tag, requeue)
             * Setting requeue=false triggers the Dead Letter Exchange logic.
             */
            channel.basicReject(deliveryTag, false); 
        };

        channel.basicConsume(MAIN_QUEUE, false, deliverCallback, consumerTag -> { });
    }
}
