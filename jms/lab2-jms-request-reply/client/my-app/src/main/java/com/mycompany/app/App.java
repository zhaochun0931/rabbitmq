package com.mycompany.app;


import javax.jms.Queue;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {
        RMQConnectionFactory factory = new RMQConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin"); // <-- Add this
        factory.setPassword("password"); // <-- Add this

        try (Connection connection = factory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 1. Create the request message
            TextMessage requestMessage = session.createTextMessage("Hello, I need the account balance.");

            // 2. Set up reply-to queue and start listening on it
            // NOTE: As discussed, this line causes your 18,000 queue bloat!
            Destination replyQueue = session.createTemporaryQueue();
            
            requestMessage.setJMSReplyTo(replyQueue);
            MessageConsumer responseConsumer = session.createConsumer(replyQueue);
            
            // 3. Set up BlockingQueue to bridge async listener to sync thread
            BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);
            responseConsumer.setMessageListener(msg -> queue.add(msg));

            // 4. Send request message
            MessageProducer producer = session.createProducer(session.createQueue("request.queue"));
            producer.send(requestMessage);
            System.out.println("Sent request, waiting for response...");

            try {
                // 5. Wait response for 5 seconds
                Message response = queue.poll(5, TimeUnit.SECONDS);
                
                if (response != null && response instanceof TextMessage) {
                    System.out.println("Received response: " + ((TextMessage) response).getText());
                } else {
                    System.out.println("Request timed out or invalid response.");
                }
                
                // 6. Close the response consumer
                responseConsumer.close();
                
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for response: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
