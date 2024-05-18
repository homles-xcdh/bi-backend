package com.demo.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * excel工具类
 */
@Slf4j
public class ExcelUtils {

    /**
     * 读取excel并转成csv
     *
     * @param multipartFile excel文件
     * @return csv字符串
     */
    public static String readExcelToCsv(MultipartFile multipartFile) {
        List<Map<Integer, String>> excelData = null;
        try {
            //读取excel
            excelData = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0).doReadSync();
        } catch (IOException e) {
            log.error("读取excel失败 error = {}", e.getMessage());
            throw new RuntimeException(e);
        }
        if (CollUtil.isEmpty(excelData)) {
            return "";
        }
        //拼接 csv
        StringBuilder sb = new StringBuilder();
        //读取表头
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) excelData.get(0);
        List<String> headerDataList = headerMap.values().stream().filter(ObjectUtils :: isNotEmpty).collect(Collectors.toList());
        sb.append(StringUtils.join(headerDataList, ",")).append("\n");
        //读取数据
        for (int i = 1; i < excelData.size(); i++) {
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) excelData.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils :: isNotEmpty).collect(Collectors.toList());
            sb.append(StringUtils.join(dataList, ",")).append("\n");
        }
        log.info("csv = {}", sb);
        return sb.toString();
    }
}