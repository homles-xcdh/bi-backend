package com.demo.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

/**
 *
 */
public class DirectConsumer {

    private static final String EXCHANGE_NAME = "direct_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        //创建队列，绑定交换机
        String queueName1 = "hong-queue";
        channel.queueDeclare(queueName1, true, false, false, null);
        //指定路由键：routingKey
        channel.queueBind(queueName1, EXCHANGE_NAME, "hong");

        String queueName2 = "hei-queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, EXCHANGE_NAME, "hei");

        //处理消息
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [小红] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [小黑] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        //消费消息
        channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> {
        });
        channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> {
        });
    }
}