package com.demo.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.springbootinit.model.dto.chart.ChartQueryRequest;
import com.demo.springbootinit.model.entity.Chart;
import com.demo.springbootinit.model.vo.chart.ChartVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author aliterc
 * @description 针对表【chart(图标信息表)】的数据库操作Service
 * @createDate 2023-07-03 16:29:49
 */
public interface ChartService extends IService<Chart> {

    /**
     * 获取脱敏的图标信息
     *
     * @param chart 图标信息
     * @return 脱敏后的图标信息
     */
    ChartVO getChartVO(Chart chart);

    /**
     * 获取脱敏的图标信息列表
     *
     * @param chartList 图标信息
     * @return 脱敏后的图标信息列表
     */
    List<ChartVO> getChartVO(List<Chart> chartList);

    /**
     * 获取查询条件
     *
     * @param chartQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    /**
     * 处理图表状态
     *
     * @param chartId     图表id
     * @param chartStatus 图表状态
     */
    void handleChartStatus(long chartId, Integer chartStatus);

    /**
     * 处理图表状态
     *
     * @param chartId     图表id
     * @param chartStatus 图表状态
     */
    boolean handleUpdateChartStatus(long chartId, Integer chartStatus);

    /**
     * 图表更新失败
     *
     * @param chartId     图表id
     * @param execMessage 异常信息
     */
    void handleChartUpdateError(long chartId, String execMessage);

    /**
     * 组装用户输入（数据库）
     *
     * @param chart 图表数据
     * @return csvData
     */
    String handleUserInput(Chart chart);

    /**
     * 根据 id 获取图表数据
     *
     * @param chartId 图表id
     * @return chart
     */
    Chart getChartById(long chartId);

    /**
     * 手动重试 AI 生成图表
     *
     * @param chartId 图表id
     * @param request
     * @return boolean
     */
    boolean reloadChartByAi(long chartId, HttpServletRequest request);


}
