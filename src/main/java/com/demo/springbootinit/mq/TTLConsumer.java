package com.demo.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 消息过期机制
 */
public class TTLConsumer {

    private static final String QUEUE_NAME = "queue_ttl";
    private static final String MESSAGE_TTL_NAME = "message_ttl";

    public static void main(String[] argv) throws Exception {
        testQueueTTL();
        //testMessageTL();
    }

    private static void testQueueTTL() throws IOException, TimeoutException {
        //建立链接、创建频道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //定义如何处理消息
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            };
            //消费消息，持续阻塞
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        }
    }

    private static void testMessageTL() throws IOException, TimeoutException {
        //建立链接、创建频道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            //定义如何处理消息
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            };
            //消费消息，持续阻塞
            channel.basicConsume(MESSAGE_TTL_NAME, true, deliverCallback, consumerTag -> {
            });
        }
    }
}