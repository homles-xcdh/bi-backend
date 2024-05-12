package com.demo.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 图标信息创建请求
 */
@Data
public class ChartAddRequest implements Serializable {
    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图标名称
     */
    private String chartName;

    /**
     * 图标数据
     */
    private String chartData;

    /**
     * 图标类型
     */
    private String chartType;

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     * 生成状态[0:等待1:运行中2:失败3:成功]
     */
    private Integer genStatus;

    /**
     * 执行信息
     */
    private String execMessage;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}