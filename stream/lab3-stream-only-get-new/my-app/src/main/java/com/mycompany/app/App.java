package com.mycompany.app;

import com.rabbitmq.stream.Address;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class App {

    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void main(String[] args) throws Exception {

        // 1. Address Resolver (Fix for node2 redirection)
        com.rabbitmq.stream.AddressResolver resolver = address ->
                new Address("localhost", address.port());

        // 2. Build the Environment
        try (Environment environment = Environment.builder()
                .host("localhost")
                .port(5552)
                .username("admin")
                .password("password")
                .addressResolver(resolver)
                .build()) {

            System.out.println("[" + LocalTime.now().format(dtf) + "] [*] Connected to Stream.");
            System.out.println("[*] Waiting for NEW messages (ignoring history)...");

            // 3. Initialize the Consumer with .next()
            environment.consumerBuilder()
                    .stream("high_speed_stream")
                    .offset(OffsetSpecification.next()) // <--- THE KEY CHANGE
                    .messageHandler((context, message) -> {
                        String body = new String(message.getBodyAsBinary(), StandardCharsets.UTF_8);
                        
                        System.out.println("[" + LocalTime.now().format(dtf) + "] [x] Received NEW: " + body);
                        System.out.println("    Offset: " + context.offset());
                    })
                    .build();

            // 4. Keep the app alive to listen
            // Since we are waiting for future events, we keep the thread running.
            while (true) {
                Thread.sleep(1000);
            }
        }
    }
}
