package com.demo.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 消息消费者
 *
 * @author lwx
 * @since 2023/7/9 15:27
 */
@Slf4j
@Component
public class MyMessageConsumer {

    //指定程序监听的消息队列和确认机制
    @RabbitListener(queues = { "test_queue" }, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(value = AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {} deliveryTag = {}", message, deliveryTag);
        try {
            //确认消息
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            //todo 消息失败放入到死信队列中
            log.error("消息确认失败 message={}", e.getMessage());
        }
    }
}