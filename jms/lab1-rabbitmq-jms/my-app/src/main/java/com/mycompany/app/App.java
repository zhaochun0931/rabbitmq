package com.mycompany.app;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

public class App {

    public static void main(String[] args) {
        try {
            // 1. Set up the RabbitMQ Connection Factory
            RMQConnectionFactory factory = new RMQConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("admin");
            factory.setPassword("password");
            factory.setVirtualHost("/");
            factory.setPort(5672);

            // 2. Create Connection and Session
            Connection connection = factory.createConnection();
            connection.start();
            
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 3. Automatically create the queue
            // The RabbitMQ JMS client will declare "my_new_queue" on the server if it doesn't exist
            Queue queue = session.createQueue("my_new_queue");

            // 4. Create the Producer attached to our new queue
            MessageProducer producer = session.createProducer(queue);

            // 5. Send 100 Text Messages
            for (int i = 1; i <= 100; i++) {
                String payload = "Auto-created Queue Message " + i;
                TextMessage message = session.createTextMessage(payload);
                
                producer.send(message);
                System.out.println("Sent: " + payload);
            }

            // 6. Clean up resources
            producer.close();
            session.close();
            connection.close();
            
            System.out.println("Finished sending 100 messages.");

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
