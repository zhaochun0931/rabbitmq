package com.example.myproject;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class RabbitPublisher implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public Queue myQueue() {
        return new Queue("my_queue", true, false, false);
    }

    // 1. Set up the callback listener right after Spring initializes the template
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
            String id = (correlationData != null) ? correlationData.getId() : "Unknown";

            if (ack) {
                System.out.println("-> [ACK] Broker confirmed message ID: " + id);
            } else {
                System.err.println("-> [NACK] Broker REJECTED message ID: " + id + ". Reason: " + cause);
            }
        });
    }

    @Override
    public void run(String... args) throws Exception {
        String baseMessage = "Hello World!";

        System.out.println("Sending 10 messages with Publisher Confirms enabled...");

        for (int i = 1; i <= 10; i++) {
            String message = baseMessage + " - Msg #" + i;

            // 2. Attach a unique ID to this message delivery tracking object
            CorrelationData correlationData = new CorrelationData("msg-id-" + i);

            // 3. Pass the correlation data as the 4th parameter
            rabbitTemplate.convertAndSend("", "my_queue", (Object) message, correlationData);

            System.out.println("Sent: " + message);
        }

        System.out.println("\nAll 10 messages sent. Waiting for asynchronous ACKs from the broker...\n");
    }
}