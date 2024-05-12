package com.demo.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 自动分析图表请求
 */
@Data
public class GenChartByAiRequest implements Serializable {

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图标名称
     */
    private String chartName;

    /**
     * 图标类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}