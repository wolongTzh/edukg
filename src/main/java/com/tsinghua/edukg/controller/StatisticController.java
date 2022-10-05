package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.GetEntityListParam;
import com.tsinghua.edukg.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "api/statistic")
@Slf4j
public class StatisticController {

    @Autowired
    StatisticService statisticService;

    /**
     * 获取产品词条
     *
     * @return
     */
    @GetMapping(value = "getEntityList")
    public WebResInfo getEntityList(GetEntityListParam param) {
        List<Entity> entities = statisticService.getEntityList(param);
        WebResInfo webResInfo = new WebResInfo();
        webResInfo.setCode(0);
        webResInfo.setData(entities);
        webResInfo.setMessage("success");
        return webResInfo;
    }
}
