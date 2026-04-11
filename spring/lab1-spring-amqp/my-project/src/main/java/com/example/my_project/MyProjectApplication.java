package com.example.my_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class MyProjectApplication implements CommandLineRunner {



    private final RabbitTemplate rabbitTemplate;

    // Inject RabbitTemplate via constructor
    public MyProjectApplication(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(MyProjectApplication.class, args);
    }

    // 1. Create the Queue
    // RabbitAdmin will find this bean and bind it to the default exchange with the routing key "myQueue".
    @Bean
    public Queue myQueue() {
        // Creates a non-durable queue. It will be removed when RabbitMQ stops.
        return new Queue("myQueue", false);
    }

    // 2. Consume the Message
    // Listens to "myQueue" and prints the incoming message.
    @RabbitListener(queues = "myQueue")
    public void listen(String in) {
        System.out.println("Message read from myQueue : " + in);
    }

    // 3. Send the Message
    // CommandLineRunner executes after the application starts up.
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Sending message...");
        rabbitTemplate.convertAndSend("myQueue", "Hello, world!");
    }
    

}
