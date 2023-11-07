// producer

import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.AMQP.Confirm.SelectOk;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;


public class App {
    public final static String EXCHANGE_NAME="EXCHANGE_MQ";
    public final static String QUEUE_NAME="qq2";

    public static void  main(String[] args) throws Exception, InterruptedException {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置主机、用户名、密码和客户端端口号
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");
        factory.setPort(5672);

        //创建一个新的连接 即TCP连接
        Connection connection = factory.newConnection();

        //创建一个通道
        Channel channel = connection.createChannel();

        //创建一个交换机
        // channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout",true);

        //创建一个队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME,"");
        for(int i =0;i<1000;i++){
            String message = "Hello World" + (i);
            //发送消息
            channel.basicPublish(EXCHANGE_NAME,"",MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
            System.out.println("Message send success:" + message);
        }

    }
}
