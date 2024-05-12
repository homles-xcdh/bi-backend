package com.demo.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.springbootinit.mapper.ChartMapper;
import com.demo.springbootinit.model.entity.Chart;
import com.demo.springbootinit.service.ChartService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}




