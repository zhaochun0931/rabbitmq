package com.example.my_project;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyProjectApplication implements CommandLineRunner {

    private final MessageSender messageSender;

    public MyProjectApplication(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public static void main(String[] args) {
        SpringApplication.run(MyProjectApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        int totalMessages = 1_000_000;
        System.out.println("Starting to send " + totalMessages + " messages...");
        
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= totalMessages; i++) {
            messageSender.sendMessage("Hello, world! Message #" + i);
            
            // Optional: Print a progress update every 100,000 messages
            if (i % 100000 == 0) {
                System.out.println("Sent " + i + " messages...");
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Finished sending all messages in " + (endTime - startTime) + " ms.");
    }
}
