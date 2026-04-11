package com.example.RpcServer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RpcServerListener {

    @RabbitListener(queues = RabbitMQConfig.REQUEST_QUEUE)
    public String processRequest(String request) {
        System.out.println(">>> SERVER: Received request -> " + request);
        
        // Simulate processing...
        String response = request.toUpperCase() + " [APPROVED BY SERVER PROJECT]";
        
        System.out.println(">>> SERVER: Sending response -> " + response);
        return response; 
    }
}
