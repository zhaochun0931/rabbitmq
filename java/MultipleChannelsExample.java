import com.rabbitmq.client.*;

public class MultipleChannelsExample {

    public static void main(String[] args) throws Exception {

        // Create a connection factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        // Create a connection
        try (Connection connection = factory.newConnection()) {
            
            // Create multiple channels from the same connection
            Channel channel1 = connection.createChannel();
            Channel channel2 = connection.createChannel();
            
            // Declare a queue on channel 1
            String queue1 = "queue1";
            channel1.queueDeclare(queue1, false, false, false, null);
            
            // Declare a queue on channel 2
            String queue2 = "queue2";
            channel2.queueDeclare(queue2, false, false, false, null);

            // Publish a message to channel 1
            String message1 = "Hello from channel 1!";
            channel1.basicPublish("", queue1, null, message1.getBytes());

            // Publish a message to channel 2
            String message2 = "Hello from channel 2!";
            channel2.basicPublish("", queue2, null, message2.getBytes());

            // Consume messages from channel 1
            channel1.basicConsume(queue1, true, new DefaultConsumer(channel1) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    String message = new String(body);
                    System.out.println("Received on channel 1: " + message);
                }
            });

            // Consume messages from channel 2
            channel2.basicConsume(queue2, true, new DefaultConsumer(channel2) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    String message = new String(body);
                    System.out.println("Received on channel 2: " + message);
                }
            });

            // Wait to receive messages
            Thread.sleep(10000); // Sleep to allow consumption of messages

            // Close channels
            channel1.close();
            channel2.close();
        }
    }
}
