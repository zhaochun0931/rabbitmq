package com.example.rabbitmq_consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {

    @RabbitListener(queues = "myQueue")
    public void listen(String in) {
        System.out.println("Receiver Project read: " + in);
    }
}
