package com.demo.springbootinit.bizmq;

import com.demo.springbootinit.common.ErrorCode;
import com.demo.springbootinit.constant.BiMqConstant;
import com.demo.springbootinit.exception.BusinessException;
import com.demo.springbootinit.model.entity.Chart;
import com.demo.springbootinit.model.enums.ChartStatusEnum;
import com.demo.springbootinit.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * bi 死信队列消息消费者
 *
 * @author lwx
 * @since 2023/7/9 15:27
 */
@Slf4j
@Component
public class BIMessageDlxConsumer {

    @Resource
    private ChartService chartService;

    //指定程序监听的消息队列和确认机制
    @RabbitListener(queues = { BiMqConstant.BI_CHART_DLX_QUEUE_NAME }, ackMode = "MANUAL")
    public void biReceiveDlxMessage(String message, Channel channel, @Header(value = AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("biReceiveDlxMessage message = {} deliveryTag = {}", message, deliveryTag);
        try {
            if (StringUtils.isBlank(message)) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
            }

            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            if (ObjectUtils.isEmpty(chart)) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表不存在");
            }

            //如果图表状态已经为 fail 直接确认消息 （防止重复更新）
            if (ChartStatusEnum.FAIL.getValue().intValue() == chart.getGenStatus().intValue()) {
                //确认消息
                channel.basicAck(deliveryTag, false);
                return;
            }

            //修改图表状态为 fail
            boolean updateRes = chartService.handleUpdateChartStatus(chartId, ChartStatusEnum.FAIL.getValue());
            if (!updateRes) {
                log.info("处理死信队列消息失败,失败图表id = {}", chart.getId());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }

            //确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("其他异常 error = {}", e.getMessage());
            }
            log.error("任务处理失败 message = {} deliveryTag = {} error = {}", message, deliveryTag, e.getMessage());
        }
    }
}