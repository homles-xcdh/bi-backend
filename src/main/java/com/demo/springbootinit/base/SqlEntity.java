package com.demo.springbootinit.base;

import lombok.Data;

/**
 * SqlEntity sql实体
 *
 * @author lwx
 * @since 2023/7/5 16:02
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