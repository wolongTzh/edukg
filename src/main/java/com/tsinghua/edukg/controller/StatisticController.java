package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.controller.utils.StatisticControllerUtil;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.SourceAndCount;
import com.tsinghua.edukg.model.VO.UpdateTotalStatusVO;
import com.tsinghua.edukg.model.params.GetAllEntityListParam;
import com.tsinghua.edukg.model.params.GetEntityListParam;
import com.tsinghua.edukg.model.WebResInfo;
import com.tsinghua.edukg.model.params.SubjectStatParam;
import com.tsinghua.edukg.service.StatisticService;
import com.tsinghua.edukg.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * statistic controller
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@RestController
@RequestMapping(value = "api/statistic")
@Slf4j
public class StatisticController {

    @Autowired
    StatisticService statisticService;

    /**
     * 实体模糊查询接口（分页）
     *
     * @return
     */
    @GetMapping(value = "getEntityList")
    public WebResInfo getEntityList(GetEntityListParam param) {
        StatisticControllerUtil.validAndFillGetEntityListParam(param);
        List<Entity> entities = statisticService.getEntityList(param);
        return WebUtil.successResult(entities);
    }

    /**
     * 实体模糊查询接口（无分页）
     *
     * @return
     */
    @GetMapping(value = "getAllEntityList")
    public WebResInfo getAllEntityList(GetAllEntityListParam param) {
        StatisticControllerUtil.validGetAllEntityListParam(param);
        List<Entity> entities = statisticService.getAllEntityList(param);
        return WebUtil.successResult(entities);
    }

    /**
     * 统计各数据来源的实体数量
     *
     * @return
     */
    @GetMapping(value = "sourceStat")
    public WebResInfo sourceStat() {
        List<SourceAndCount> sourceAndCountList = statisticService.updateSource();
        return WebUtil.successResult(sourceAndCountList);
    }

    /**
     * 更新全图谱统计信息
     *
     * @return
     */
    @GetMapping(value = "totalStat")
    public WebResInfo totalStat() {
        UpdateTotalStatusVO updateTotalStatusVO = statisticService.updateTotalStatus();
        return WebUtil.successResult(updateTotalStatusVO);
    }

    /**
     * 更新某学科统计信息
     *
     * @return
     */
    @GetMapping(value = "subjectStat")
    public WebResInfo subjectStat(SubjectStatParam param) {
        StatisticControllerUtil.validsubjectStatParam(param);
        UpdateTotalStatusVO updateTotalStatusVO = statisticService.updateSubjectStatus(param);
        return WebUtil.successResult(updateTotalStatusVO);
    }
}
