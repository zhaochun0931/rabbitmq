package com.example.my_project;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MessageReceiver {

    // Thread-safe counter
    private final AtomicInteger messageCount = new AtomicInteger(0);

    @RabbitListener(queues = "myQueue")
    public void listen(String in) {
        int currentCount = messageCount.incrementAndGet();
        
        // Print progress every 100,000 messages received
        if (currentCount % 100000 == 0) {
            System.out.println("Receiver processed " + currentCount + " messages so far.");
        }
    }
}
