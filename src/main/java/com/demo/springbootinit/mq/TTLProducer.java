package com.demo.springbootinit.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 消息过期机制：
 * <li>给队列中所有消息指定过期时间 {@link this#testQueueTTL()}</li>
 * <li>给某条消息指定过期时间 {@link this#testMessageTTL()}</li>
 * <p>
 * 结论：
 * <li>如果在过期时间内，还没有消费者取消息，消息才会过期。</li>
 * <li>如果消息已经接收到，但是没确认，是不会过期的。</li>
 */
public class TTLProducer {

    private static final String QUEUE_NAME = "queue_ttl";
    private static final String MESSAGE_TTL_NAME = "message_ttl";

    public static void main(String[] argv) throws Exception {
//        testQueueTTL();
        testMessageTTL();
    }

    private static void testQueueTTL() throws IOException, TimeoutException {
        //创建链接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        //建立链接、创建频道
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", 5000);
            channel.queueDeclare(QUEUE_NAME, false, false, false, args);
            String message = "test queue ttl";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }

    private static void testMessageTTL() throws IOException, TimeoutException {
        //创建链接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        //建立链接、创建频道
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(MESSAGE_TTL_NAME, false, false, false, null);
            String message = "test message ttl";
            //消息过期时间
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().expiration("5000").build();
            channel.basicPublish("", MESSAGE_TTL_NAME, properties, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}