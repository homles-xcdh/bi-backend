package com.demo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.springbootinit.bizmq.BiMessageProducer;
import com.demo.springbootinit.common.ErrorCode;
import com.demo.springbootinit.constant.BiConstant;
import com.demo.springbootinit.constant.CommonConstant;
import com.demo.springbootinit.exception.BusinessException;
import com.demo.springbootinit.exception.ThrowUtils;
import com.demo.springbootinit.manager.RedisLimiterManager;
import com.demo.springbootinit.mapper.ChartMapper;
import com.demo.springbootinit.model.dto.chart.ChartQueryRequest;
import com.demo.springbootinit.model.entity.Chart;
import com.demo.springbootinit.model.entity.User;
import com.demo.springbootinit.model.enums.ChartStatusEnum;
import com.demo.springbootinit.model.vo.chart.ChartVO;
import com.demo.springbootinit.service.ChartService;
import com.demo.springbootinit.service.UserService;
import com.demo.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aliterc
 * @description 针对表【chart(图标信息表)】的数据库操作Service实现
 * @since 2023-07-03 16:29:49
 */
@Slf4j
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

    @Resource
    private BiMessageProducer biMessageProducer;

    @Resource
    private UserService userService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Override
    public ChartVO getChartVO(Chart chart) {
        if (chart == null) {
            return null;
        }
        ChartVO chartVO = new ChartVO();
        BeanUtils.copyProperties(chart, chartVO);
        return chartVO;
    }

    @Override
    public List<ChartVO> getChartVO(List<Chart> chartList) {
        if (CollectionUtils.isEmpty(chartList)) {
            return new ArrayList<>();
        }
        return chartList.stream().map(this :: getChartVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String chartName = chartQueryRequest.getChartName();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
//        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartName), "chartName", chartName);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void handleChartStatus(long chartId, Integer chartStatus) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setGenStatus(chartStatus);
        if (!this.updateById(updateChart)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新图表状态失败");
//            this.handleChartUpdateError(updateChart.getId(), "更新图表状态失败");
        }
    }

    @Override
    public boolean handleUpdateChartStatus(long chartId, Integer chartStatus) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setGenStatus(chartStatus);
        return this.updateById(updateChart);
    }

    @Override
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setGenStatus(ChartStatusEnum.FAIL.getValue());
        chart.setExecMessage(execMessage);
        if (!this.updateById(chart)) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }

    @Override
    public String handleUserInput(Chart chart) {
        ThrowUtils.throwIf(ObjectUtils.isEmpty(chart), ErrorCode.NOT_FOUND_ERROR);
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();
        // 用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append(goal).append("\n");
        userInput.append("原始数据：").append("\n");
        //csv数据
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }

    @Override
    public Chart getChartById(long chartId) {
        ThrowUtils.throwIf(chartId < 0, ErrorCode.PARAMS_ERROR);
        Chart chart = this.getById(chartId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(chart), ErrorCode.NOT_FOUND_ERROR, "图表数据不存在");
        return chart;
    }

    @Override
    public boolean reloadChartByAi(long chartId, HttpServletRequest request) {
        ThrowUtils.throwIf(chartId < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        final String key = BiConstant.BI_REDIS_LIMITER_KEY + loginUser.getId();
        // 限流判断
        redisLimiterManager.doRateLimit(key);
        //发送消息
        biMessageProducer.sendMessage(String.valueOf(chartId));
        return true;
    }
}




