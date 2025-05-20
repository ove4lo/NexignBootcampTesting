package validateCDRtoBRTnew.additionally;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitMqClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static final String EXCHANGE = "cdr.exchange";
    private static final String ROUTING_KEY = "cdr.routing.key";
    private static final String RESPONSE_QUEUE = "cdr.response.queue";

    private Connection connection;
    public Channel channel;

    public RabbitMqClient() throws IOException, TimeoutException {
        connect();
    }

    private void connect() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public void publishToExchange(String message) throws IOException {
        channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT, true);
        channel.basicPublish(EXCHANGE, ROUTING_KEY, null, message.getBytes(StandardCharsets.UTF_8));
    }

    public String getSingleResponseMessage() throws IOException {
        GetResponse response = channel.basicGet(RESPONSE_QUEUE, true);
        return response != null ? new String(response.getBody(), StandardCharsets.UTF_8) : null;
    }

    public String waitForResponseMessage(int timeoutSeconds) throws IOException {
        int waited = 0;
        while (waited < timeoutSeconds) {
            String msg = getSingleResponseMessage();
            if (msg != null) return msg;
            try {
                Thread.sleep(1000);
                waited++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Ожидание было прервано", e);
            }
        }
        return null;
    }

    public void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) channel.close();
        if (connection != null && connection.isOpen()) connection.close();
    }

    // Геттеры для возможного расширения
    public static String getExchange() {
        return EXCHANGE;
    }

    public static String getRoutingKey() {
        return ROUTING_KEY;
    }

    public static String getResponseQueue() {
        return RESPONSE_QUEUE;
    }
}

