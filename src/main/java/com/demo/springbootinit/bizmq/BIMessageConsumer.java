package com.demo.springbootinit.bizmq;

import com.demo.springbootinit.common.ErrorCode;
import com.demo.springbootinit.constant.BiConstant;
import com.demo.springbootinit.constant.BiMqConstant;
import com.demo.springbootinit.exception.BusinessException;
import com.demo.springbootinit.manager.AiManager;
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
 * bi 消息消费者
 */
@Slf4j
@Component
public class BIMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    //指定程序监听的消息队列和确认机制
    @RabbitListener(queues = { BiMqConstant.BI_CHART_QUEUE_NAME }, ackMode = "MANUAL")
    public void biReceiveMessage(String message, Channel channel, @Header(value = AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("biReceiveMessage message = {} deliveryTag = {}", message, deliveryTag);
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

            //修改图表状态为 running
            boolean updateRes = chartService.handleUpdateChartStatus(chartId, ChartStatusEnum.RUNNING.getValue());
            if (!updateRes) {
                channel.basicNack(deliveryTag, false, false);
                chartService.handleChartUpdateError(chartId, "更新图表状态执行中失败");
                return;
            }

            //向AI提问
            String userInput = chartService.handleUserInput(chart);
            String aiRes = aiManager.doChat(BiConstant.BI_MODEL_ID, userInput);
            //处理AI返回数据，得到 图表数据 和 分析建议
            String[] aiData = aiRes.split(BiConstant.AI_SPLIT_STR);
            log.info("aiData len = {} data = {}", aiData.length, aiRes);
            if (aiData.length < 3) {
                channel.basicNack(deliveryTag, false, false);
                chartService.handleChartUpdateError(chartId, "Ai生成有误");
                return;
            }
            String genChart = aiData[ 1 ].trim();
            String genResult = aiData[ 2 ].trim();

            //更新 图表数据
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            updateChart.setGenChart(genChart);
            updateChart.setGenResult(genResult);
            updateChart.setGenStatus(ChartStatusEnum.SUCCEED.getValue());
            if (!chartService.updateById(updateChart)) {
                channel.basicNack(deliveryTag, false, false);
                chartService.handleChartUpdateError(chartId, "更新图表失败");
            }

            //确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败 error = {}", ex.getMessage());
            }
            log.error("任务处理失败 message = {} deliveryTag = {} error = {}", message, deliveryTag, e.getMessage());
        }
    }
}