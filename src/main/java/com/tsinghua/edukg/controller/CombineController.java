package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.api.feign.QAFeignService;
import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.api.model.QAResult;
import com.tsinghua.edukg.controller.utils.AlgorithmControllerUtil;
import com.tsinghua.edukg.model.VO.CombineLinkingVO;
import com.tsinghua.edukg.model.VO.CombineQaVO;
import com.tsinghua.edukg.model.WebResInfo;
import com.tsinghua.edukg.model.params.TotalSearchParam;
import com.tsinghua.edukg.service.CombineService;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.RuleHandler;
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

    @Autowired
    QAFeignService qaFeignService;

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
        if(RuleHandler.judgeConsistentQuestion(qaParam.getQuestion())) {
            CombineQaVO consistentVO = new CombineQaVO();
            String answer = "";
            String subject = "";
            StringBuilder sb = new StringBuilder();
            boolean start = true;
            QAResult qaResult = null;
            try {
                qaResult = qaFeignService.qaRequest(CommonUtil.entityToMutiMap(qaParam)).getAnswerData();
                subject = qaResult.getSubject();
                if(subject.equals("王安石")) {
                    System.out.println(1);
                }
                if(!qaParam.getQuestion().contains(subject)) {
                    throw new Exception();
                }
            }
            catch (Exception e) {
                CombineQaVO combineQaVO = combineService.totalQaForTest(qaParam);
                return WebUtil.successResult(combineQaVO);
            }
            for(char c : qaParam.getQuestion().toCharArray()) {
                if(c == '？' || c == '?' || c == ',' || c == '，') {
                    CombineQaVO curAnswer;
                    if(start) {
                        curAnswer = combineService.totalQaForTest(new QAParam(sb.toString()));
                        start = false;
                    }
                    else {
                        curAnswer = combineService.totalQaForTest(new QAParam(subject + sb.toString()));
                    }
                    if(curAnswer.getAnswer() != null) {
                        answer += curAnswer.getAnswer().getAnswerValue() + "|";
                    }
                    else if(curAnswer.getQaesGrepVO().size() != 0) {
                        answer += curAnswer.getQaesGrepVO().get(0).getText() + "|";
                    }
                    sb.delete(0, sb.length());
                }
                else {
                    sb.append(c);
                }
            }
            if(sb.length() != 0) {
                CombineQaVO curAnswer;
                curAnswer = combineService.totalQaForTest(new QAParam(subject + sb.toString()));
                if(curAnswer.getAnswer() != null) {
                    answer += curAnswer.getAnswer().getAnswerValue() + "|";
                }
                else if(curAnswer.getQaesGrepVO().size() != 0) {
                    answer += curAnswer.getQaesGrepVO().get(0).getText() + "|";
                }
                sb.delete(0, sb.length());
            }
            consistentVO.setConsistentAnswer(answer);
            QAResult subjectAnswer = new QAResult();
            subjectAnswer.setSubject(subject);
            consistentVO.setAnswer(subjectAnswer);
            return WebUtil.successResult(consistentVO);
        }
        CombineQaVO combineQaVO = combineService.totalQaForTest(qaParam);
        return WebUtil.successResult(combineQaVO);
    }
}
