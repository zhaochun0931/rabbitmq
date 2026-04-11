package com.example.my_project;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Creates a non-durable queue. It will be removed when RabbitMQ stops.
    @Bean
    public Queue myQueue() {
        return new Queue("myQueue", false);
    }
}
