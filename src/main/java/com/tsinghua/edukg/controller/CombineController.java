package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.controller.utils.AlgorithmControllerUtil;
import com.tsinghua.edukg.model.VO.CombineLinkingVO;
import com.tsinghua.edukg.model.VO.CombineQaVO;
import com.tsinghua.edukg.model.WebResInfo;
import com.tsinghua.edukg.model.params.TotalSearchParam;
import com.tsinghua.edukg.service.CombineService;
import com.tsinghua.edukg.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * 综合搜索 controller
 *
 * @author tanzheng
 * @date 2022/10/26
 */
@RestController
@RequestMapping(value = "api/common")
@Slf4j
public class CombineController {

    @Autowired
    CombineService combineService;

    /**
     * 实体链接综合搜索
     *
     * @return
     */
    @PostMapping(value = "totalSearch")
    public WebResInfo totalSearch(@RequestBody TotalSearchParam param) throws IllegalAccessException, IOException {
        CombineLinkingVO combineLinkingVO = combineService.totalSearch(param);
        return WebUtil.successResult(combineLinkingVO);
    }

    /**
     * 问答综合搜索
     *
     * @return
     */
    @PostMapping(value = "totalQa")
    public WebResInfo totalQa(@RequestBody QAParam qaParam) throws IllegalAccessException, IOException, ExecutionException, InterruptedException {
        AlgorithmControllerUtil.validInputQuestionParam(qaParam);
        CombineQaVO combineQaVO = combineService.totalQa(qaParam);
        return WebUtil.successResult(combineQaVO);
    }

    /**
     * 问答综合搜索
     *
     * @return
     */
    @PostMapping(value = "totalQaForTest")
    public WebResInfo totalQaForTest(@RequestBody QAParam qaParam) throws IllegalAccessException, IOException, ExecutionException, InterruptedException {
        AlgorithmControllerUtil.validInputQuestionParam(qaParam);
        CombineQaVO combineQaVO = combineService.totalQaForTest(qaParam);
        return WebUtil.successResult(combineQaVO);
    }
}
