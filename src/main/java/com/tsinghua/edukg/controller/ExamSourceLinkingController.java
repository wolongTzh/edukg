package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.controller.utils.ExamSourceControllerUtil;
import com.tsinghua.edukg.enums.BusinessTypeEnum;
import com.tsinghua.edukg.model.VO.GetExamSourceVO;
import com.tsinghua.edukg.model.VO.QAESGrepVO;
import com.tsinghua.edukg.model.WebResInfo;
import com.tsinghua.edukg.model.params.GetExamSourceParam;
import com.tsinghua.edukg.service.ExamSourceLinkingService;
import com.tsinghua.edukg.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 试题资源读取 controller
 *
 * @author tanzheng
 * @date 2022/11/3
 */
@RestController
@RequestMapping(value = "api/resource")
@Slf4j
public class ExamSourceLinkingController {

    @Autowired
    ExamSourceLinkingService examSourceLinkingService;

    /**
     * 通过uri查询试题资源
     *
     * @return
     */
    @GetMapping(value = "getFromUri")
    public WebResInfo getFromUri(GetExamSourceParam param) throws IOException {
        ExamSourceControllerUtil.validGetFromUri(param);
        GetExamSourceVO examSourceVO = examSourceLinkingService.getExamSourceFromUri(param);
        return WebUtil.successResult(examSourceVO);
    }

    /**
     * 通过文本查询试题资源
     *
     * @return
     */
    @GetMapping(value = "findQuestion")
    public WebResInfo getFromText(GetExamSourceParam param) throws IOException {
        ExamSourceControllerUtil.validGetFromText(param);
        GetExamSourceVO examSourceVO = examSourceLinkingService.getExamSourceFromText(param, BusinessTypeEnum.LINKING);
        return WebUtil.successResult(examSourceVO);
    }

    /**
     * IRQA
     *
     * @return
     */
    @GetMapping(value = "irqa")
    public WebResInfo irqa(String question) throws IOException, ExecutionException, InterruptedException {
        List<QAESGrepVO> qaesGrepVOList = examSourceLinkingService.getAnswerFromIRQA(question);
        return WebUtil.successResult(qaesGrepVOList);
    }
}
