package com.mycompany.app;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;



/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");


ConnectionFactory factory = new ConnectionFactory();
        
        // Update these with your cluster details
        factory.setHost("localhost");
        factory.setUsername("admin"); 
        factory.setPassword("password");
        factory.setVirtualHost("/");

// Ensure the connection itself doesn't block us at 2047
        factory.setRequestedChannelMax(0);

        List<Connection> connections = new ArrayList<>();
        int totalChannels = 0;

        try {
            // Open 3 connections to spread the 4000+ channels
            for (int connIdx = 1; connIdx <= 3; connIdx++) {
                System.out.println("Opening Connection #" + connIdx);
                Connection conn = factory.newConnection();
                connections.add(conn);

                // Try to open 2000 channels per connection
                for (int chanIdx = 1; chanIdx <= 2000; chanIdx++) {
                    try {
                        conn.createChannel();
                        totalChannels++;

                        if (totalChannels % 500 == 0) {
                            System.out.println("Total channels across all connections: " + totalChannels);
                        }
                        
                        // If we hit 4000, the NEXT one should trigger the error
                        if (totalChannels > 4005) break;

                    } catch (IOException e) {
                        System.err.println("\n--- SUCCESS: ERROR REPRODUCED ---");
                        System.err.println("Total count reached: " + totalChannels);
                        System.err.println("Error: " + e.getMessage());
                        System.err.println("---------------------------------");
                        throw e; // Exit
                    }
                }
            }
        } catch (Exception e) {
            // This is where your "reached the maximum allowed user limit" will appear
            System.out.println("Final Catch: " + e.getMessage());
        } finally {
            Thread.sleep(5000); // Give you time to check the UI
            for (Connection c : connections) { if(c.isOpen()) c.close(); }
        }



    }
}
