import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StreamQueuePublisher {

    private final static String STREAM_QUEUE_NAME = "my_stream_queue";  // Stream Queue name
    private final static String EXCHANGE_NAME = "my_exchange";         // Optional exchange

    public static void main(String[] argv) throws Exception {
        // Set up the connection factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");  // RabbitMQ host (localhost by default)
        factory.setPort(5672);         // Default RabbitMQ port
        factory.setUsername("admin");  // Default RabbitMQ username
        factory.setPassword("password");  // Default RabbitMQ password

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare a stream queue (RabbitMQ 9.7+)
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-queue-type", "stream"); // Specify the queue type as "stream"

            // Stream queues must be durable (cannot be non-durable)
            // Declare the queue as durable (true), and set the 'x-queue-type' to 'stream'
            channel.queueDeclare(STREAM_QUEUE_NAME, true, false, false, arguments);
            System.out.println(" [*] Stream queue declared: " + STREAM_QUEUE_NAME);

            // Optionally, declare an exchange and bind the queue to it
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.queueBind(STREAM_QUEUE_NAME, EXCHANGE_NAME, "routing_key");

            // Create the message to send
            String message = "Hello, RabbitMQ Stream Queue!";

            // Set the message properties (optional)
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .contentType("text/plain")
                    .deliveryMode(2) // Persistent message
                    .build();

            // Publish the message to the stream queue
            channel.basicPublish(EXCHANGE_NAME, "routing_key", properties, message.getBytes());
            System.out.println(" [x] Sent '" + message + "' to stream queue: " + STREAM_QUEUE_NAME);
        }
    }
}
