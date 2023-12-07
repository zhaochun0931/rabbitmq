// declare the quorum queue

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

import java.nio.charset.StandardCharsets;


public class App {

    public static void main(String[] args) throws Exception {


        // 使用ConnectionFactory创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
// 注意这里写5672，是amqp协议端口
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("password");
        factory.setVirtualHost("test");


        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            Map<String, Object> qtype = new HashMap<>();
            qtype.put("x-queue-type", "quorum");


            for (int i = 1; i <= 6000; i++) {
                System.out.println("hello" + i);
                channel.queueDeclare("qq" +i , true, false, false, qtype);


            }

//            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//            String message = "Hello World!";
//            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
//            System.out.println(" [x] Sent '" + message + "'");
        }

    }
}
