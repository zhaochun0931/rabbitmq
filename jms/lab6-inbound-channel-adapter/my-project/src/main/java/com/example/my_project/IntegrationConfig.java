package com.example.my_project;

// 1. Core RabbitMQ connection import
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

// 2. Core Spring Integration imports
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
public class IntegrationConfig {

    // 1. Define the Inbound Integration Flow
    @Bean
    public IntegrationFlow amqpInboundFlow(ConnectionFactory connectionFactory) {
        return IntegrationFlow
                // Step 1: Listen to the queue defined in RabbitMQConfig
                .from(Amqp.inboundAdapter(connectionFactory, RabbitMQConfig.QUEUE_NAME))

                // Step 2: Safely transform the payload, handling both raw bytes (from UI) and Strings
                .transform(Object.class, payload -> {
                    if (payload instanceof byte[]) {
                        // Convert byte array from RabbitMQ UI into a String
                        return new String((byte[]) payload);
                    }
                    // Otherwise, treat it as a normal String
                    return payload.toString();
                })

                // Step 3: Handle the message (this is where your business logic goes)
                .handle(message -> {
                    System.out.println("=====================================");
                    System.out.println("Inbound Adapter Received: " + message.getPayload());
                    System.out.println("Message Headers: " + message.getHeaders());
                    System.out.println("=====================================");
                })
                .get();
    }
}
