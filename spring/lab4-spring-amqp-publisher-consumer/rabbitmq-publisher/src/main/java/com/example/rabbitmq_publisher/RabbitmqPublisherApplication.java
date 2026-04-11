package com.example.rabbitmq_publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class RabbitmqPublisherApplication implements CommandLineRunner {

    private final MessageSender messageSender;

    public RabbitmqPublisherApplication(MessageSender messageSender) {
        this.messageSender = messageSender;
    }


    @Override
    public void run(String... args) throws Exception {
        messageSender.sendOneHundredMessages();
    }


    public static void main(String[] args) {
        SpringApplication.run(RabbitmqPublisherApplication.class, args);
    }

}
