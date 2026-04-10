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

import com.rabbitmq.jms.admin.RMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class App {

    public static void main(String[] args) {
        try {
            // 1. Set up connection
            RMQConnectionFactory factory = new RMQConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("admin"); 
            factory.setPassword("password");
            // THE MAGIC FIX:
            // Tell the server NOT to declare the reply-to queue,
            // preventing the 'resource_locked' exception on exclusive temp queues.
            factory.setDeclareReplyToDestination(false);
            

            Connection connection = factory.createConnection();
            connection.start();
            
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 2. Listen to the main "request.queue"
            Destination requestQueue = session.createQueue("request.queue");
            MessageConsumer consumer = session.createConsumer(requestQueue);
            
            System.out.println("RPC Server is listening on request.queue...");

            // 3. Process incoming messages asynchronously
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message request) {
                    try {
                        if (request instanceof TextMessage) {
                            System.out.println("Received request: " + ((TextMessage) request).getText());

                            // 4. Extract the "Return Address" (Temp Queue or Direct Reply-To)
                            Destination replyTo = request.getJMSReplyTo();

                            if (replyTo != null) {
                                // 5. Process the business logic
                                String answer = "Account balance is $1,500.00";
                                TextMessage responseMessage = session.createTextMessage(answer);

                                // IMPORTANT: JMS Best Practice is to copy the Correlation ID from Request to Response
                                responseMessage.setJMSCorrelationID(request.getJMSCorrelationID());

                                // 6. Send the reply back to the specific return address
                                MessageProducer producer = session.createProducer(replyTo);
                                producer.send(responseMessage);
                                producer.close(); // Close this producer when done
                                
                                System.out.println("Sent reply to " + replyTo.toString());
                            } else {
                                System.err.println("Received a message with no JMSReplyTo destination!");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // Keep the main thread alive so the listener can run in the background
            Thread.sleep(600000); // Run for 10 minutes

            consumer.close();
            session.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
