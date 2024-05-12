package com.demo.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

/**
 *
 */
public class TopicConsumer {

    private static final String EXCHANGE_NAME = "topic_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        //创建队列，绑定交换机
        String queueName1 = "frontend-queue";
        channel.queueDeclare(queueName1, true, false, false, null);
        //1、 #.前端.后端.# 2、 *.前度.后端. 3、*.前度.后端.*
        channel.queueBind(queueName1, EXCHANGE_NAME, "*.前端.*");
        String queueName2 = "backend-queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, EXCHANGE_NAME, "*.后端.*");
        String queueName3 = "product-queue";
        channel.queueDeclare(queueName3, true, false, false, null);
        channel.queueBind(queueName3, EXCHANGE_NAME, "#.产品.#");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //处理任务
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [员工A] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [员工B] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [员工C] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        //消费任务
        channel.basicConsume(queueName1, true, deliverCallback1, consumerTag -> {
        });
        channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {
        });
        channel.basicConsume(queueName3, true, deliverCallback3, consumerTag -> {
        });
    }
}