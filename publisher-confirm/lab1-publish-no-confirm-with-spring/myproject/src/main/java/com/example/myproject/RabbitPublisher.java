package com.example.myproject;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RabbitPublisher implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 1. Declare the durable queue as a Spring Bean (replaces channel.queueDeclare)
    @Bean
    public Queue myQueue() {
        // Parameters: name, durable, exclusive, autoDelete
        return new Queue("my_queue", true, false, false);
    }

    // 2. This method automatically executes exactly once when the Spring Boot App starts
    @Override
    public void run(String... args) throws Exception {
        String baseMessage = "Hello World!";

        System.out.println("Sending 10 messages blindly via Spring Boot...");

        // 3. Loop exactly 10 times
        for (int i = 1; i <= 10; i++) {
            String message = baseMessage + " - Msg #" + i;

            // Fire-and-forget routing via default exchange ("")
            // RabbitTemplate handles string-to-byte serialization natively
            rabbitTemplate.convertAndSend("", "my_queue", message);
            
            System.out.println("Sent: " + message);
        }

        System.out.println("\nAll 10 messages sent continuously.");
    }
}
