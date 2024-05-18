package com.demo.springbootinit.model.vo.chart;

import lombok.Data;

/**
 * Ai返回结果
 */
@Data
public class BiResponse {
    /**
     * 图表id
     */
    private Long chartId;

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
}