import com.rabbitmq.client.*;

public class BasicGet {

    private final static String QUEUE_NAME = "myQueue"; // Your queue name

    public static void main(String[] argv) throws Exception {
        // Create a connection factory and configure it
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ host (localhost if running locally)
        factory.setPort(5672);       // RabbitMQ port (default is 5672)
        factory.setUsername("admin"); // Default RabbitMQ username
        factory.setPassword("password"); // Default RabbitMQ password

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare the queue (ensure it exists before consuming)
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // Fetch a single message using basic.get
            GetResponse response = channel.basicGet(QUEUE_NAME, true); // 'true' auto-acknowledges the message

            if (response != null) {
                // Message was retrieved, process it
                String message = new String(response.getBody(), "UTF-8");
                System.out.println("Received message: " + message);
            } else {
                // No message was available in the queue
                System.out.println("No messages available.");
            }
        }
    }
}
