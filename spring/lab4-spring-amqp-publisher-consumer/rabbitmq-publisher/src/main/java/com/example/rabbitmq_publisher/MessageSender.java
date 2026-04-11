package com.example.rabbitmq_publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {

    private final RabbitTemplate rabbitTemplate;

    public MessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOneHundredMessages() {
        System.out.println("--- Starting batch of 100 messages ---");
        for (int i = 1; i <= 100; i++) {
            String message = "Hello from Sender Project! Message #" + i;
            System.out.println("Sending: " + message);
            rabbitTemplate.convertAndSend("myQueue", message);
        }
        System.out.println("--- Finished sending 100 messages ---");
    }
}
