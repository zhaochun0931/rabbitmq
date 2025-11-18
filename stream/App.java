import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;



public class App {

    public String getGreeting() {
        return "Hello World!";
    }

    private final static String CLASSIC_QUEUE_NAME = "classic-queue";
    private final static String QUORUM_QUEUE_NAME = "quorum-queue";
    private final static String STREAM_QUEUE_NAME = "stream-queue";

    public static void main(String[] argv) throws Exception {

        System.out.println(new App().getGreeting());

        // Connect to RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");
        factory.setHost("haproxy");
        factory.setPort(8888);

        // Message date formatter
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()

        ) {
            
            /* -----------------------------
                1. Declare CLASSIC QUEUE
               ----------------------------- */
            channel.queueDeclare(
                    CLASSIC_QUEUE_NAME,
                    true,     // durable
                    false,    // exclusive
                    false,    // auto-delete
                    null      // no args = classic queue
            );

            System.out.println("✔ Classic Queues created successfully");

            /* -----------------------------
                2. Declare Stream QUEUE
               ----------------------------- */




            // Declare a stream queue (RabbitMQ 9.7+)
            Map<String, Object> streamArgs = new HashMap<>();
            streamArgs.put("x-queue-type", "stream"); // Specify the queue type as "stream"

            channel.queueDeclare(
                    STREAM_QUEUE_NAME,
                    true,
                    false,
                    false,
                    streamArgs
            );

            System.out.println("✔ Stream Queues created successfully");

            /* -----------------------------
                3. Send 100 messages to each queue
               ----------------------------- */

            for (int i = 1; i <= 100; i++) {
                String now = LocalDateTime.now().format(dtf);

                // Message body
                String message = "Message #" + i + " at " + now;

                // Publish
                channel.basicPublish("", CLASSIC_QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
                channel.basicPublish("", STREAM_QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));


                System.out.println(" [x] Sent: " + message);
            }
        }

    }
}
