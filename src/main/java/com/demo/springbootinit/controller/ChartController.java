package com.demo.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.springbootinit.annotation.AuthCheck;
import com.demo.springbootinit.bizmq.BiMessageProducer;
import com.demo.springbootinit.common.BaseResponse;
import com.demo.springbootinit.common.DeleteRequest;
import com.demo.springbootinit.common.ErrorCode;
import com.demo.springbootinit.common.ResultUtils;
import com.demo.springbootinit.constant.BiConstant;
import com.demo.springbootinit.constant.FileConstant;
import com.demo.springbootinit.constant.UserConstant;
import com.demo.springbootinit.exception.BusinessException;
import com.demo.springbootinit.exception.ThrowUtils;
import com.demo.springbootinit.manager.AiManager;
import com.demo.springbootinit.manager.RedisLimiterManager;
import com.demo.springbootinit.model.dto.chart.*;
import com.demo.springbootinit.model.entity.Chart;
import com.demo.springbootinit.model.entity.User;
import com.demo.springbootinit.model.enums.ChartStatusEnum;
import com.demo.springbootinit.model.vo.chart.BiResponse;
import com.demo.springbootinit.model.vo.chart.ChartVO;
import com.demo.springbootinit.service.ChartService;
import com.demo.springbootinit.service.UserService;
import com.demo.springbootinit.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 图标信息接口
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;

    // region 增删改查

    /**
     * 创建图标信息（管理员）
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartAddRequest.setUserId(loginUser.getId());
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(chart.getId());
    }

    /**
     * 删除图标信息
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = chartService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新图标信息
     *
     * @param chartUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest, HttpServletRequest request) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        boolean result = chartService.updateById(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新图标信息
     *
     * @param updateGenChartRequest
     * @param request
     * @return
     */
    @PostMapping("/update/gen")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> updateGenChart(@RequestBody UpdateGenChartRequest updateGenChartRequest, HttpServletRequest request) {
        if (updateGenChartRequest == null || updateGenChartRequest.getId() == null
                || updateGenChartRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(updateGenChartRequest, chart);
        boolean result = chartService.updateById(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取图标信息
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        ThrowUtils.throwIf(chart == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(chart);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ChartVO> getChartVOById(long id, HttpServletRequest request) {
        BaseResponse<Chart> response = getChartById(id, request);
        Chart chart = response.getData();
        return ResultUtils.success(chartService.getChartVO(chart));
    }

    /**
     * 分页获取图标信息列表（仅管理员）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取图标信息封装列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ChartVO>> listChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        if (UserConstant.DEFAULT_ROLE.equals(loginUser.getUserRole())) {
            chartQueryRequest.setUserId(loginUserId);
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), chartService.getQueryWrapper(chartQueryRequest));
        Page<ChartVO> chartVOPage = new Page<>(current, size, chartPage.getTotal());
        List<ChartVO> chartVO = chartService.getChartVO(chartPage.getRecords());
        chartVOPage.setRecords(chartVO);
        return ResultUtils.success(chartVOPage);
    }

    // endregion

    @PostMapping("/gen")
    public BaseResponse<BiResponse> getChartByAi(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        String goal = genChartByAiRequest.getGoal();
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        String chartName = genChartByAiRequest.getChartName();
        ThrowUtils.throwIf(StringUtils.isBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称为空");
        String chartType = genChartByAiRequest.getChartType();
        //校验文件
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > FileConstant.MAX_FILE_SIZE, ErrorCode.SYSTEM_ERROR, "文件超过1M");
        String originalFilename = multipartFile.getOriginalFilename();
        String fileSuffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!BiConstant.VALID_FILE_SUFFIX_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式有误");

        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        final String key = BiConstant.BI_REDIS_LIMITER_KEY + loginUser.getId();
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit(key);
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
        String csvData = ExcelUtils.readExcelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        //向AI提问
        String aiRes = aiManager.doChat(BiConstant.BI_MODEL_ID, userInput.toString());
        //截取AI数据
        final String str = "=>=>=>";
        String[] aiData = aiRes.split(str);
        log.info("aiData len = {} data = {}", aiData.length, aiRes);
        ThrowUtils.throwIf(aiData.length < 3, ErrorCode.SYSTEM_ERROR, "AI生成错误");
        String genChart = aiData[ 1 ].trim();
        String genResult = aiData[ 2 ].trim();
        //插入数据
        Chart chart = new Chart();
        chart.setGoal(userGoal);
        chart.setChartName(chartName);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setGenStatus(ChartStatusEnum.SUCCEED.getValue());
        ThrowUtils.throwIf(!chartService.save(chart), ErrorCode.SYSTEM_ERROR, "图表保存失败");
        //返回AI对话数据
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        return ResultUtils.success(biResponse);
    }

    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> getChartByAiAsync(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        String goal = genChartByAiRequest.getGoal();
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        String chartName = genChartByAiRequest.getChartName();
        ThrowUtils.throwIf(StringUtils.isBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称为空");
        String chartType = genChartByAiRequest.getChartType();
        //校验文件
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > FileConstant.MAX_FILE_SIZE, ErrorCode.SYSTEM_ERROR, "文件超过1M");
        String originalFilename = multipartFile.getOriginalFilename();
        String fileSuffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!BiConstant.VALID_FILE_SUFFIX_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式有误");

        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        final String key = BiConstant.BI_REDIS_LIMITER_KEY + loginUser.getId();
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit(key);

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
        String csvData = ExcelUtils.readExcelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //在ai对话前将图表数据入库 状态为 wait
        Chart chart = new Chart();
        chart.setGoal(userGoal);
        chart.setChartName(chartName);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        chart.setGenStatus(ChartStatusEnum.WAIT.getValue());
        ThrowUtils.throwIf(!chartService.save(chart), ErrorCode.SYSTEM_ERROR, "图表保存失败");

        Long chartId = chart.getId();
        //将AI对话放到线程池中去执行

        CompletableFuture.runAsync(() -> {
            //修改图表状态为 running
            chartService.handleChartStatus(chartId, ChartStatusEnum.RUNNING.getValue());

            //向AI提问
            String aiRes = aiManager.doChat(BiConstant.BI_MODEL_ID, userInput.toString());
            //处理AI返回数据
            final String str = "=>=>=>";
            String[] aiData = aiRes.split(str);
            log.info("aiData len = {} data = {}", aiData.length, aiRes);
            ThrowUtils.throwIf(aiData.length < 3, ErrorCode.SYSTEM_ERROR, "AI生成错误");
            String genChart = aiData[ 1 ].trim();
            String genResult = aiData[ 2 ].trim();

            //更新 图表数据
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            updateChart.setGenChart(genChart);
            updateChart.setGenResult(genResult);
            updateChart.setGenStatus(ChartStatusEnum.SUCCEED.getValue());
            if (!chartService.updateById(updateChart)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新图表失败");
            }
        }, threadPoolExecutor).exceptionally(e -> {
            log.error("AI生成错误 chartId = {} userId = {} error = {}", chartId, chart.getUserId(), e.getMessage());
            //修改图表状态为 fail
            chartService.handleChartStatus(chart.getId(), ChartStatusEnum.FAIL.getValue());
            return null;
        });
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chartId);
        return ResultUtils.success(biResponse);
    }

    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> getChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        String goal = genChartByAiRequest.getGoal();
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        String chartName = genChartByAiRequest.getChartName();
        ThrowUtils.throwIf(StringUtils.isBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称为空");
        String chartType = genChartByAiRequest.getChartType();
        //校验文件
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > FileConstant.MAX_FILE_SIZE, ErrorCode.SYSTEM_ERROR, "文件超过1M");
        String originalFilename = multipartFile.getOriginalFilename();
        String fileSuffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!BiConstant.VALID_FILE_SUFFIX_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式有误");

        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        final String key = BiConstant.BI_REDIS_LIMITER_KEY + loginUser.getId();
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit(key);

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
        String csvData = ExcelUtils.readExcelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //在ai对话前将图表数据入库 状态为 wait
        Chart chart = new Chart();
        chart.setGoal(userGoal);
        chart.setChartName(chartName);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        chart.setGenStatus(ChartStatusEnum.WAIT.getValue());
        boolean saveRes = chartService.save(chart);
        ThrowUtils.throwIf(!saveRes, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        Long newChartId = chart.getId();
        //向消息队列发送消息
        biMessageProducer.sendMessage(String.valueOf(newChartId));

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(newChartId);
        return ResultUtils.success(biResponse);
    }

    @GetMapping("/reload/gen")
    public BaseResponse<Boolean> reloadChartByAi(long chartId, HttpServletRequest request) {
        return ResultUtils.success(chartService.reloadChartByAi(chartId, request));
    }
}
