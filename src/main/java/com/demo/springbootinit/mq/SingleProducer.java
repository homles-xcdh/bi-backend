package com.demo.springbootinit.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 一个生产者
 */
public class SingleProducer {

    private static final String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        //创建链接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
//        factory.setUsername();
//        factory.setPassword();
//        factory.setPort();
        //建立链接、创建频道
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            /**
             * 创建消息队列 {@link Channel#queueDeclare(String, boolean, boolean, boolean, Map)}
             * 参数：
             * queue：队列名称
             * durable：消息队列重启后，消息是否丢失（持久化）
             * exclusive：是否只允许当前这个创建消息队列的连接操作消息队列
             * autoDelete：没有人用队列后，是否要删除队列
             * arguments：是否要携带参数
             */
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            /**
             * 发送消息 {@link Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])}
             * 参数：
             * exchange：交换机名称
             * routingKey：发送到那个队列名
             * props：消息的其他属性 – 路由标头等
             * body：消息正文
             */
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}