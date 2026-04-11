package com.example.my_project;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
public class IntegrationConfig {

    // 1. The Gateway that the rest of your application will call
    @MessagingGateway(defaultRequestChannel = "outboundChannel")
    public interface MyMessageGateway {
        void sendMessage(String data);
    }

    // 2. The Integration Flow with the Outbound Channel Adapter
    @Bean
    public IntegrationFlow amqpOutboundFlow(RabbitTemplate rabbitTemplate) {
        return IntegrationFlow.from("outboundChannel")
                // Here is the Outbound Channel Adapter configuration!
                .handle(Amqp.outboundAdapter(rabbitTemplate)
                        .exchangeName(RabbitMQConfig.EXCHANGE_NAME)
                        .routingKey(RabbitMQConfig.ROUTING_KEY))
                .get();
    }
}
