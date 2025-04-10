import com.rabbitmq.client.*;

import java.io.IOException; // Import IOException

public class BasicConsume {

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

            // Step 3: Declare the queue (ensure it exists before consuming)
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // Step 4: Set up a consumer to handle the messages
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8"); // Convert message body to string
                    System.out.println("Received message: " + message);
                }
            };

            // Step 5: Start consuming messages from the queue
            channel.basicConsume(QUEUE_NAME, true, consumer);

            // Keep the program running to listen for messages (e.g., using a simple sleep or while loop)
            System.out.println("Waiting for messages...");
            while (true) {
                // Keep the program alive
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
