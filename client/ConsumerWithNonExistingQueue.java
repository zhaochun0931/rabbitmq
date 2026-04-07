import com.rabbitmq.client.*;

public class ConsumerWithNonExistingQueue {

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            String nonExistingQueue = "non_existing_queue"; // Queue that doesn't exist

            // Try to consume from a non-existing queue
            channel.basicConsume(nonExistingQueue, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    String message = new String(body);
                    System.out.println("Received: " + message);
                }
            });

        } catch (Exception e) {
            // This will catch errors like trying to consume from a non-existing queue
            System.err.println("Error: " + e.getMessage());
        }
    }
}
