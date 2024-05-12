package com.demo.springbootinit.constant;

/**
 * 文件常量
 */
public interface FileConstant {

    /**
     * COS 访问地址
     * todo 需替换配置
     */
    String COS_HOST = "https://yupi.icu";

    /**
     * 限制 文件大小 1M
     */
    Long MAX_FILE_SIZE = 1024 * 1024L;
}
