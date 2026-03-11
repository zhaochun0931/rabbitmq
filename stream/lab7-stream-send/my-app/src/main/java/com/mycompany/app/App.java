package com.mycompany.app;

import com.rabbitmq.stream.Address;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Producer;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void main(String[] args) throws Exception {

        // 1. Address Resolver to handle local node redirection
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

            // 3. Create the stream (Permanent - no retention policy)
            environment.streamCreator()
                    .stream("high_speed_stream")
                    .create();

            // 4. Initialize the Producer
            Producer producer = environment.producerBuilder()
                    .stream("high_speed_stream")
                    .build();

            System.out.println("[" + LocalTime.now().format(dtf) + "] [*] Sending 100 messages...");

            CountDownLatch confirmLatch = new CountDownLatch(100);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 1; i <= 100; i++) {
                String timestamp = LocalTime.now().format(dtf);
                String body = "Stream Message #" + i + " | Permanent at: " + timestamp;

                producer.send(
                    producer.messageBuilder()
                            .properties().creationTime(System.currentTimeMillis())
                            .messageBuilder()
                            .addData(body.getBytes(StandardCharsets.UTF_8))
                            .build(),
                    confirmationStatus -> {
                        if (confirmationStatus.isConfirmed()) {
                            successCount.incrementAndGet();
                        }
                        confirmLatch.countDown();
                    }
                );
            }

            // 5. Wait for confirmations and close
            confirmLatch.await(15, TimeUnit.SECONDS);
            System.out.println("[" + LocalTime.now().format(dtf) + "] [√] Sent " + successCount.get() + " messages.");

            producer.close();
        } // This closes the Environment via try-with-resources
    } // This closes main
} // This closes App
