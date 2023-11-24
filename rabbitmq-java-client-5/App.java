// declare the classic queue

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;


public class App {

    public static void main(String[] args) throws Exception {


        // 使用ConnectionFactory创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
// 注意这里写5672，是amqp协议端口
        factory.setPort(5672);
        factory.setUsername("default_user_ApKY84B1xLiQcI_MuMY");
        factory.setPassword("0Muv2_nVTx5GCCQRz8OGETODr8pe5uJx");
        factory.setVirtualHost("test");


        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            for (int i = 2; i <= 10; i++) {
                System.out.println("hello");
                channel.queueDeclare("qq" +i , false, false, false, null);


            }

//            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//            String message = "Hello World!";
//            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
//            System.out.println(" [x] Sent '" + message + "'");
        }

    }
}
