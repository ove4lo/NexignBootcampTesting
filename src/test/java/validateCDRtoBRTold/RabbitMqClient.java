package validateCDRtoBRTold;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RabbitMqClient {
    private final Connection connection;
    private final Channel channel;
    private final String requestQueueName;
    private final String responseQueueName;

    public RabbitMqClient(String host, int port, String username, String password,
                          String requestQueue, String responseQueue)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.requestQueueName = requestQueue;
        this.responseQueueName = responseQueue;

        channel.queueDeclare(requestQueueName, false, false, false, null);
        channel.queueDeclare(responseQueueName, false, false, false, null);
    }

    public String sendAndReceive(String message, String correlationId)
            throws IOException, InterruptedException {
        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        String ctag = channel.basicConsume(responseQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {
                if (properties.getCorrelationId().equals(correlationId)) {
                    response.offer(new String(body, StandardCharsets.UTF_8));
                }
            }
        });

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .replyTo(responseQueueName)
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes(StandardCharsets.UTF_8));

        String result = response.poll(10, TimeUnit.SECONDS);
        channel.basicCancel(ctag);
        return result;
    }

    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }
}