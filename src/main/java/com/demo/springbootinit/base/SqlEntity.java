package com.demo.springbootinit.base;

import lombok.Data;

/**
 * SqlEntity sql实体
 */
@Data
public class SqlEntity {

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 字段类型
     */
    private String columnType;
}