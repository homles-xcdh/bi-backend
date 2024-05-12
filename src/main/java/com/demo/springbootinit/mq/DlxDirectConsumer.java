package com.demo.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列
 */
public class DlxDirectConsumer {

    private static final String DEAD_EXCHANGE_NAME = "dlx_direct_exchange";
    private static final String WORK_EXCHANGE_NAME = "dlx_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

        //指定死信队列参数
        Map<String, Object> args1 = new HashMap<>();
        //要绑定那个死信交换机
        args1.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        //指定死信要发送到那个死信队列
        args1.put("x-dead-letter-routing-key", "jack");
        //创建队列
        String queueName1 = "tom_queue";
        channel.queueDeclare(queueName1, false, false, false, args1);
        channel.queueBind(queueName1, WORK_EXCHANGE_NAME, "tom");

        Map<String, Object> args2 = new HashMap<>();
        args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args2.put("x-dead-letter-routing-key", "tom");
        String queueName2 = "jack_queue";
        channel.queueDeclare(queueName2, false, false, false, args2);
        channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "jack");

        //处理消息
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            //拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [jack] reject '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            //拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [tom] reject '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        //消费消息
        channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> {
        });
        channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> {
        });
    }
}