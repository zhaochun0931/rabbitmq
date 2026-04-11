package com.example.RpcServer;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REQUEST_QUEUE = "rpc.request.queue";

    @Bean
    public Queue rpcRequestQueue() {
        return new Queue(REQUEST_QUEUE, false);
    }
}
