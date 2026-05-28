package com.mycompany.app;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

public class App {

    // Points to your local RabbitMQ broker container/instance
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "JavaExampleClient_" + System.currentTimeMillis();
    private static final String TOPIC = "java/mqtt/demo";

    public static void main(String[] args) {
        System.out.println("Starting MQTT v5 App...");

        try {
            // 1. Initialize the MQTT v5 client
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);

            // 2. Configure MQTT v5 connection options
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setCleanStart(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(60);

            // FIX: Explicitly supply credentials to bypass RabbitMQ's strict access rules
            options.setUserName("admin");
            options.setPassword("password".getBytes());

            // 3. Set up MQTT v5 callbacks
            client.setCallback(new MqttCallback() {
                @Override
                public void disconnected(MqttDisconnectResponse disconnectResponse) {
                    System.out.println("\n[Alert] Disconnected from broker. Reason: " + disconnectResponse.getReasonString());
                }

                @Override
                public void mqttErrorOccurred(MqttException exception) {
                    System.out.println("\n[Error] MQTT Error: " + exception.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("\n[Received] Topic: " + topic);
                    System.out.println("[Received] Message: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttToken token) {
                    System.out.println("[System] Delivery confirmed.");
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    System.out.println("[System] Connection established to: " + serverURI);
                }

                @Override
                public void authPacketArrived(int reasonCode, MqttProperties properties) {
                    // Used for advanced authentication methods if required
                }
            });

            // 4. Connect to RabbitMQ
            System.out.println("Connecting to broker: " + BROKER_URL);
            client.connect(options);
            System.out.println("Connected successfully!");

            // 5. Subscribe to the topic
            System.out.println("Subscribing to topic: " + TOPIC);
            client.subscribe(TOPIC, 1); // Subscribing with QoS 1

            // 6. Publish a test message
            String payload = "Hello from Java MQTT v5 Client!";
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1); // QoS 1: At least once delivery

            System.out.println("Publishing message: " + payload);
            client.publish(TOPIC, message);

            // Keep alive for 5 seconds to let the loopback message process and show in console
            Thread.sleep(5000);

            // 7. Disconnect and close gracefully
            System.out.println("\nDisconnecting...");
            client.disconnect();
            client.close();
            System.out.println("Disconnected smoothly.");

        } catch (MqttException e) {
            System.err.println("MQTT Error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}