package com.example.my_project;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {

    // Listens to "myQueue" and prints the incoming message.
    @RabbitListener(queues = "myQueue")
    public void listen(String in) {
        System.out.println("Message read from myQueue : " + in);
    }
}
