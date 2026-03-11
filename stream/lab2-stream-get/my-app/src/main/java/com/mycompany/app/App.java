package com.mycompany.app;

import com.rabbitmq.stream.Address;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

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

            System.out.println(" [*] Connected to Stream. Replaying ALL history...");
            
            // We use an AtomicInteger to count how many we actually find
            AtomicInteger messageCount = new AtomicInteger(0);

            // 3. Initialize the Consumer
            // .offset(OffsetSpecification.first()) ensures we start at Offset 0 every time
            environment.consumerBuilder()
                    .stream("high_speed_stream")
                    .offset(OffsetSpecification.first())
                    .messageHandler((context, message) -> {
                        String body = new String(message.getBodyAsBinary(), StandardCharsets.UTF_8);
                        int currentCount = messageCount.incrementAndGet();
                        
                        System.out.println(" [Received #" + currentCount + "] " + body + " | Offset: " + context.offset());
                    })
                    .build();

            // 4. Keep the app alive to let the stream finish "pouring" in
            // Since we don't know exactly how many messages are in the stream total, 
            // we wait for a few seconds to see them all.
            System.out.println(" [*] Waiting 10 seconds for all historical data...");
            Thread.sleep(10000); 
            
            System.out.println(" [√] Finished. Total messages read this session: " + messageCount.get());
        }
    }
}
