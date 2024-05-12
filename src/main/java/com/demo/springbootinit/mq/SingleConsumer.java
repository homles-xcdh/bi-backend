package com.demo.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

/**
 * 单个消费者
 */
public class SingleConsumer {

    private static final String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        //建立链接、创建频道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //创建消息队列（注意：同名称消息队列已经存在了再次执行声明队列方法参数必须和之前一致）
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            //定义如何处理消息
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            };
            //消费消息，持续阻塞
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
        }
    }
}