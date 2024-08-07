import java.io.*;
import java.security.*;
import javax.net.ssl.*;

import com.rabbitmq.client.*;

public class App {

    public static void main(String[] args) throws Exception {

        char[] keyPassphrase = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream("/tmp/tls.p12"), keyPassphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keyPassphrase);




        char[] trustPassphrase = "password".toCharArray();
        KeyStore tks = KeyStore.getInstance("JKS");
        tks.load(new FileInputStream("/tmp/tls-truststore.jks"), trustPassphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(tks);

        SSLContext c = SSLContext.getInstance("TLSv1.2");
        c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5671);
        factory.setUsername("admin");
        factory.setPassword("password");
        factory.setVirtualHost(VIRTUAL_HOST);
        factory.useSslProtocol(c);
//        factory.enableHostnameVerification();

        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        channel.queueDeclare("rabbitmq-java-test", false, false, false, null);
        channel.basicPublish("", "rabbitmq-java-test", null, "Hello, World, rabbitmq-java-test".getBytes());

        channel.queueDeclare("qq-test", false, false, false, null);
        channel.basicPublish("", "qq-test", null, "Hello, World, qq-test".getBytes());

        GetResponse chResponse = channel.basicGet("rabbitmq-java-test", false);
        if (chResponse == null) {
            System.out.println("No message retrieved");
        } else {
            byte[] body = chResponse.getBody();
            System.out.println("Received: " + new String(body));
        }




        GetResponse chResponse2 = channel.basicGet("qq-test", false);
        if (chResponse2 == null) {
            System.out.println("No message retrieved");
        } else {
            byte[] body = chResponse2.getBody();
            System.out.println("Received: " + new String(body));
        }




        channel.close();
        conn.close();
    }
}
