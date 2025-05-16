import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class PublisherConfirmDemo {

    private final static String QUEUE_NAME = "demo-confirm-queue";

    public static void main(String[] args) throws Exception {
        // 1. Set up connection factory with authentication
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");       // Replace with your RabbitMQ host
        factory.setUsername("admin");       // Replace with your RabbitMQ username
        factory.setPassword("password");       // Replace with your RabbitMQ password
        factory.setPort(5672);              // Default AMQP port; change if needed

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 2. Declare queue
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // 3. Enable publisher confirms
            channel.confirmSelect();

            String message = "Hello RabbitMQ with Confirm and Auth!";

            // 4. Publish message
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent: '" + message + "'");

            // 5. Wait for ack
            if (channel.waitForConfirms()) {
                System.out.println(" [✓] Message successfully confirmed by the broker!");
            } else {
                System.out.println(" [✗] Message NOT confirmed!");
            }
        }
    }
}
