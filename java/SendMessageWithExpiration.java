import com.rabbitmq.client.*;
import java.util.Map;        // Import Map
import java.util.HashMap;    // Import HashMap

public class SendMessageWithExpiration {

    private final static String QUEUE_NAME = "myQueue"; // The queue name

    public static void main(String[] argv) throws Exception {
        // Step 1: Set up the connection factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ host (default is localhost)
        factory.setPort(5672);        // Default RabbitMQ port
        factory.setUsername("admin"); // Default RabbitMQ username
        factory.setPassword("password"); // Default RabbitMQ password

        // Step 2: Establish the connection and create a channel
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Step 3: Declare the queue with TTL for all messages in the queue
            Map<String, Object> args = new HashMap<>();  // HashMap for arguments
            args.put("x-message-ttl", 60000); // Queue TTL: messages will expire after 60,000 ms (1 minute)
            channel.queueDeclare(QUEUE_NAME, false, false, false, args);

            // Step 4: Set message properties with expiration (in milliseconds)
            AMQP.BasicProperties.Builder props = new AMQP.BasicProperties.Builder();
            props.expiration("10000"); // Set expiration to 10000ms (10 seconds)

            // Step 5: Publish a message to the queue
            String message = "Hello, RabbitMQ!";
            channel.basicPublish("", QUEUE_NAME, false, false, props.build(), message.getBytes());  // Fixed this line

            System.out.println(" [x] Sent '" + message + "' with expiration");

        }
    }
}
