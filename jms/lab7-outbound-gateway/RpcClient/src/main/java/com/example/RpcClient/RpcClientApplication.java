package com.example.RpcClient;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class RpcClientApplication implements CommandLineRunner {

    private final RpcClientConfig.RpcGateway rpcGateway;

    public RpcClientApplication(RpcClientConfig.RpcGateway rpcGateway) {
        this.rpcGateway = rpcGateway;
    }

    public static void main(String[] args) {
        SpringApplication.run(RpcClientApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- STARTING RPC CALL ---");
        System.out.println("<<< CLIENT: Sending request -> hello from client project!");

        // This blocks until the Server Project replies
        String response = rpcGateway.sendAndReceive("hello from client project!");

        System.out.println("<<< CLIENT: Received response -> " + response);
        System.out.println("--- RPC CALL FINISHED ---");
    }
}


