import com.rabbitmq.client.*;

public class UnackedMessage {
  
    private static final String QUEUE_NAME = "unacked-demo";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Declare queue
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // ========== Step 1: Publish 10 Messages ==========
        for (int i = 1; i <= 10; i++) {
            String message = "Message #" + i;
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println(" [→] Sent: " + message);
        }



        Thread.sleep(15000);




        // Set QoS to 1 message per consumer
        channel.basicQos(6);  // ensures unacked messages are tracked per consumer

        System.out.println(" [*] Waiting for messages. NOT acknowledging them.");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String msg = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received: " + msg);

            // Simulate processing but do NOT ack
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println(" [!] Message processed but NOT acknowledged");
            // Do NOT call channel.basicAck
        };

        // Start consuming with autoAck = false
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

        // Keep app running
        Thread.currentThread().join();
    }
}
