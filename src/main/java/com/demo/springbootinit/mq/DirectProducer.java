package com.demo.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * direct 交换机：指定消息发送到那个队列
 */
public class DirectProducer {

    private static final String EXCHANGE_NAME = "direct_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String userInput = scanner.nextLine();
                if (userInput.length() < 1) {
                    continue;
                }
                String[] inputStr = userInput.split(" ");
                String message = inputStr[ 0 ];
                String routeKey = inputStr[ 1 ];
                channel.basicPublish(EXCHANGE_NAME, routeKey, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + routeKey + "':'" + message + "'");
            }
        }
    }
}