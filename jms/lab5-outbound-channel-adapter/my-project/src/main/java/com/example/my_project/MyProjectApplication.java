package com.example.my_project;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyProjectApplication implements CommandLineRunner {

    private final IntegrationConfig.MyMessageGateway myMessageGateway;

    // Inject the Messaging Gateway
    public MyProjectApplication(IntegrationConfig.MyMessageGateway myMessageGateway) {
        this.myMessageGateway = myMessageGateway;
    }

    public static void main(String[] args) {
        SpringApplication.run(MyProjectApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Pushing messages into the Integration Flow...");

        // Call the gateway, which routes to the Outbound Channel Adapter
        myMessageGateway.sendMessage("Hello RabbitMQ via Spring Integration! #1");
        myMessageGateway.sendMessage("Hello RabbitMQ via Spring Integration! #2");

        System.out.println("Messages sent!");
    }
}
