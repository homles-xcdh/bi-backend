package com.demo.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息生产者
 */
@Component
public class MyMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     *
     * @param exchange    指定发送到那个交换机
     * @param routeingKey 发送到那个路由键
     * @param message     消息
     */
    public void sendMessage(String exchange, String routeingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routeingKey, message);
    }
}