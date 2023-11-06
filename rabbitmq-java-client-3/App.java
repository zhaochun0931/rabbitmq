//consumer2

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;
public class App {
    private static String EXCHANGE_NAME="EXCHANGE_MQ";
    private final static String QUEUE_NAME="qq2";
    public static void main(String[] args) throws Exception {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置主机
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");
        factory.setPort(5672);

        //创建一个新的连接 即TCP连接
        Connection connection = factory.newConnection();
        //创建一个通道
        final Channel channel = connection.createChannel();
        //声明队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        //创建一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout",true);
        //绑定队列到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "",null);
        System.out.println("Consumer2 Waiting Received messages");
        //DefaultConsumer类实现了Consumer接口，通过传入一个channel，
        //告诉服务器我们需要哪个channel的消息并监听channel，如果channel中有消息，就会执行回调函数handleDelivery
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Consumer2 Received '" + message + "'");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }finally{
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        //自动回复队列应答 -- RabbitMQ中的消息确认机制
        //false 不自动回复应答
        channel.basicConsume(QUEUE_NAME,false, consumer);
    }
}
