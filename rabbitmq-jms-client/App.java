import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Destination;
import javax.jms.TextMessage;
import com.rabbitmq.jms.admin.RMQConnectionFactory;

public class App {
    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new RMQConnectionFactory();
        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("queueName");
        MessageProducer producer = session.createProducer(destination);
        TextMessage message = session.createTextMessage("Hello, RabbitMQ JMS!");
        producer.send(message);
        System.out.println("Message sent successfully.");
        connection.close();
    }
}
