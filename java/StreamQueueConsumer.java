import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StreamQueueConsumer {

    private final static String STREAM_QUEUE_NAME = "my_stream_queue";  // Stream Queue name
    private final static String EXCHANGE_NAME = "my_exchange";         // Optional exchange

    public static void main(String[] argv) throws Exception {
        // Set up the connection factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");  // RabbitMQ host (localhost by default)
        factory.setPort(5672);            // Default RabbitMQ port
        factory.setUsername("admin");     // Default RabbitMQ username
        factory.setPassword("password"); // Default RabbitMQ password

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare a stream queue (RabbitMQ 3.9+)
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-queue-type", "stream");  // Specify the queue type as "stream"

            // Declare the queue as durable (true), and set the 'x-queue-type' to 'stream'
            channel.queueDeclare(STREAM_QUEUE_NAME, true, false, false, arguments);
            System.out.println(" [*] Stream queue declared: " + STREAM_QUEUE_NAME);

            // Optionally, declare an exchange and bind the queue to it
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.queueBind(STREAM_QUEUE_NAME, EXCHANGE_NAME, "routing_key");

            // Set the prefetch count for stream queues (important for stream queues)
            channel.basicQos(1);  // One message at a time per consumer

            // Define a callback to handle incoming messages
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");

                // Manually acknowledge the message after processing
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                System.out.println(" [x] Message acknowledged");
            };

            // Handle consumer cancellation
            CancelCallback cancelCallback = consumerTag -> {
                System.out.println(" [x] Consumer was cancelled");
            };

            // Start consuming messages from the stream queue
            // Set autoAck to false for manual acknowledgment
            boolean autoAck = false;  // Disable autoAck to enable manual acking
            channel.basicConsume(STREAM_QUEUE_NAME, autoAck, deliverCallback, cancelCallback);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            // Keep the consumer running
            while (true) {
                // This keeps the program running to allow the consumer to keep receiving messages
                // You can also use Thread.sleep() here if you prefer.
                Thread.sleep(1000);
            }

        }
    }
}
