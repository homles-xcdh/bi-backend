package com.demo.springbootinit.manager;


import com.demo.springbootinit.common.ErrorCode;
import com.demo.springbootinit.constant.BiConstant;
import com.demo.springbootinit.exception.ThrowUtils;
import com.github.rholder.retry.*;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * AIManager
 */
@Slf4j
@Service
public class AiManager {

    @Resource
    private YuCongMingClient client;

    /**
     * AI 对话
     *
     * @param modelId 模型id
     * @param message 问题
     * @return AI回答
     */
    public String doChat(long modelId, String message) {
        ThrowUtils.throwIf(modelId < 0, ErrorCode.PARAMS_ERROR, "AI模型id不合法");
        ThrowUtils.throwIf(StringUtils.isBlank(message), ErrorCode.PARAMS_ERROR, "问题不能为空");
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        ThrowUtils.throwIf(response.getData() == null, ErrorCode.SYSTEM_ERROR, "AI响应失败");
        //重试
        Retryer<DevChatResponse> retryer = RetryerBuilder.<DevChatResponse>newBuilder()
                //设置异常重试源 不可设置多个
                .retryIfExceptionOfType(RuntimeException.class)
                //设置根据结果重试
                .retryIfResult(res -> res.getContent().split(BiConstant.AI_SPLIT_STR).length < 3)
                //固定时长等待策略
                .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS))
                //重试指定次数停止
                .withStopStrategy(StopStrategies.stopAfterAttempt(1))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        // 重试结果: 是异常终止, 还是正常返回
                        log.info("hasException={} asResult={}", attempt.hasException(), attempt.hasResult());
                    }
                })
                .build();
        try {
            DevChatResponse devChatResponse = retryer.call(() -> client.doChat(devChatRequest).getData());
            log.info("AiResult = {}", devChatResponse.getContent());
            return devChatResponse.getContent();
        } catch (ExecutionException | RetryException e) {
            log.error("AI响应失败 error = {}", e.getMessage());
        }
        return "";
    }

    /**
     * AI对话重试
     */
    public String retryDoChat(DevChatRequest devChatRequest) {
        //重试
        Retryer<DevChatResponse> retryer = RetryerBuilder.<DevChatResponse>newBuilder()
                //设置异常重试源 不可设置多个
                .retryIfExceptionOfType(RuntimeException.class)
                //设置根据结果重试
                .retryIfResult(res -> res.getContent().split(BiConstant.AI_SPLIT_STR).length < 3)
                //固定时长等待策略
                .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS))
                //重试指定次数停止
                .withStopStrategy(StopStrategies.stopAfterAttempt(1))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        // 重试结果: 是异常终止, 还是正常返回
                        log.info("hasException={} asResult={}", attempt.hasException(), attempt.hasResult());
                    }
                })
                .build();
        try {
            DevChatResponse chatResponse = retryer.call(() -> client.doChat(devChatRequest).getData());
            ThrowUtils.throwIf(chatResponse.getContent() == null, ErrorCode.SYSTEM_ERROR, "AI响应失败");
            return chatResponse.getContent();
        } catch (ExecutionException | RetryException e) {
            log.error("AI响应失败 error = {}", e.getMessage());
        }
        return "";
    }
}