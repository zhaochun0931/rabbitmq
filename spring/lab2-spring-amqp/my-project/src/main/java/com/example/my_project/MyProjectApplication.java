package com.example.my_project;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyProjectApplication implements CommandLineRunner {

    private final MessageSender messageSender;

    // Inject the MessageSender we created above
    public MyProjectApplication(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public static void main(String[] args) {
        SpringApplication.run(MyProjectApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        messageSender.sendMessage("Hello, world!");
    }
}
