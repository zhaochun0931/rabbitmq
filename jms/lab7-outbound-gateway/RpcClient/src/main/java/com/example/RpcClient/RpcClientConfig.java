package com.example.RpcClient;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
public class RpcClientConfig {

    public static final String REQUEST_QUEUE = "rpc.request.queue";

    // 1. The Gateway interface for your application to call
    @MessagingGateway(defaultRequestChannel = "rpcOutboundChannel")
    public interface RpcGateway {
        String sendAndReceive(String data);
    }

    // 2. The Integration Flow using the Outbound Gateway
    @Bean
    public IntegrationFlow amqpRpcFlow(RabbitTemplate rabbitTemplate) {
        return IntegrationFlow.from("rpcOutboundChannel")
                .handle(Amqp.outboundGateway(rabbitTemplate)
                        .routingKey(REQUEST_QUEUE))
                .get();
    }
}
