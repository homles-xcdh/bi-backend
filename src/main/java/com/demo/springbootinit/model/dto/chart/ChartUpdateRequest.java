package com.demo.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 图标信息更新请求
 */
@Data
public class ChartUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

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

    private static final long serialVersionUID = 1L;
}