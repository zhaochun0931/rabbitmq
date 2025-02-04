import com.rabbitmq.client.*;

public class BasicSend {

    private final static String EXCHANGE_NAME = "my_exchange";
    private final static String QUEUE_NAME = "my_queue";
    private final static String ROUTING_KEY = "my_routing_key";

    public static void main(String[] args) {
        try {
            // Step 1: Create a connection to RabbitMQ
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");  // RabbitMQ broker's host (use your RabbitMQ server's address)
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                // Step 2: Declare an exchange (if it doesn't exist already)
                channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

                // Step 3: Declare a queue (if it doesn't exist already)
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);

                // Step 4: Bind the queue to the exchange with a routing key
                channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

                // Step 5: Send a message to the exchange with a routing key
                String message = "Hello RabbitMQ!";
                channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
