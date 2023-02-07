package com.tsinghua.edukg.controller;


import com.tsinghua.edukg.api.EntityLinkService;
import com.tsinghua.edukg.api.QAService;
import com.tsinghua.edukg.api.feign.EntityLinkingFeign;
import com.tsinghua.edukg.api.feign.QAFeignService;
import com.tsinghua.edukg.api.model.ApiResult;
import com.tsinghua.edukg.api.model.EntityLinkParam;
import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.api.model.QAResult;
import com.tsinghua.edukg.controller.utils.AlgorithmControllerUtil;
import com.tsinghua.edukg.model.WebResInfo;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * api controller
 *
 * @author tanzheng
 * @date 2022/10/26
 */
@RestController
@RequestMapping(value = "api/graph")
@Slf4j
public class AlgorithmController {

    @Autowired
    QAService qaService;

    @Autowired
    EntityLinkService entityLinkService;

    @Autowired
    QAFeignService qaFeignService;

    @Autowired
    EntityLinkingFeign entityLinkingFeign;

    /**
     * 问答服务访问
     *
     * @return
     */
    @PostMapping(value = "qa")
    public WebResInfo inputQuestion(@RequestBody QAParam param) throws IllegalAccessException {
        AlgorithmControllerUtil.validInputQuestionParam(param);
        ApiResult<QAResult> qaResult = qaFeignService.qaRequest(CommonUtil.entityToMutiMap(param));
        QAResult accQaResult = qaResult.getAnswerData();
        return WebUtil.successResult(accQaResult);
    }

    /**
     * 实体链接服务访问
     *
     * @return
     */
    @PostMapping(value = "parser/linking")
    public WebResInfo linking(@RequestBody EntityLinkParam param) throws IllegalAccessException {
        AlgorithmControllerUtil.validEntityLinkingParam(param);
        String qaResult = entityLinkingFeign.getEntityLinkResult(CommonUtil.entityToMutiMap(param));
        return WebUtil.successResult(qaResult);
    }
}
