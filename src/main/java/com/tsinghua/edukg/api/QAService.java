package com.tsinghua.edukg.api;

import com.tsinghua.edukg.api.feign.QAFeignService;
import com.tsinghua.edukg.api.model.ApiResult;
import com.tsinghua.edukg.api.model.qa.*;
import com.tsinghua.edukg.config.AddressConfig;
import com.tsinghua.edukg.model.SubAndPre;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.QAPreProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class QAService {

    @Resource
    QAFeignService qaFeignService;

    public QAServiceResult completeQA(String question) throws IllegalAccessException {
        List<String> subejctList = new ArrayList<>();
        SubAndPre subAndPre = QAPreProcess.mainProcess(question);
        if(!StringUtils.isEmpty(subAndPre.getSubject()) && !StringUtils.isEmpty(subAndPre.getPredicate())) {
            return allHave(subAndPre.getSubject(), subAndPre.getPredicate());
        }
        else if(!StringUtils.isEmpty(subAndPre.getSubject())) {
            return haveSubject(subAndPre.getSubject(), question);
        }
        else if(!StringUtils.isEmpty(subAndPre.getPredicate())) {
            return havePredicate(subAndPre.getPredicate(), question);
        }
        else {
            return haveNot(question);
        }
    }

    public QAServiceResult allHave(String subject, String predicate) throws IllegalAccessException {
        QAResult qaResult = qaFeignService.batchQuery(CommonUtil.entityToMutiMap(new QAQueryParam(predicate, subject))).getData();
        if(qaResult == null) {
            qaResult = new QAResult();
            qaResult.setSubject(subject);
            qaResult.setPredicate(predicate);
        }
        return new QAServiceResult(-0.001, qaResult);
    }

    public QAServiceResult haveSubject(String subject, String question) throws IllegalAccessException {
        QAParseResult qaParseResult =  qaFeignService.parse(CommonUtil.entityToMutiMap(new QAParam(question))).getData();
        String predicate = "";
        double modelScore = 0.0;
        QAParseBody qaParseBodyNoConstraint = qaParseResult.getNo_constraint();
        for(QASinglePred qaSinglePred : qaParseBodyNoConstraint.getTop_k()) {
            predicate += qaSinglePred.getPred() + ",";
        }
        String prePredicate = predicate;
        QAResult qaResult = qaFeignService.batchQuery(CommonUtil.entityToMutiMap(new QAQueryParam(predicate, subject))).getData();
        modelScore = qaParseBodyNoConstraint.getModel_score();
        if(qaResult == null) {
            predicate = "";
            QAParseBody qaParseBodyConstraint = qaParseResult.getConstraint();
            for(QASinglePred qaSinglePred : qaParseBodyConstraint.getTop_k()) {
                predicate += qaSinglePred.getPred() + ",";
            }
            qaResult = qaFeignService.batchQuery(CommonUtil.entityToMutiMap(new QAQueryParam(predicate, subject))).getData();
            modelScore = qaParseBodyConstraint.getModel_score();
        }
        if(qaResult == null) {
            qaResult = new QAResult();
            qaResult.setSubject(subject);
            qaResult.setPredicate(prePredicate);
        }
        return new QAServiceResult(modelScore, qaResult);
    }

    public QAServiceResult havePredicate(String predicate, String question) throws IllegalAccessException {
        double modelScore = 0.0;
        QAParseResult qaParseResult =  qaFeignService.parse(CommonUtil.entityToMutiMap(new QAParam(question))).getData();
        QAParseBody qaParseBodyNoConstraint = qaParseResult.getNo_constraint();
        QAResult qaResult = qaFeignService.batchQuery(CommonUtil.entityToMutiMap(new QAQueryParam(predicate, qaParseBodyNoConstraint.getTitle()))).getData();
        modelScore = qaParseBodyNoConstraint.getModel_score();
        if(qaResult == null) {
            QAParseBody qaParseBodyConstraint = qaParseResult.getConstraint();
            qaResult = qaFeignService.batchQuery(CommonUtil.entityToMutiMap(new QAQueryParam(predicate, qaParseBodyConstraint.getTitle()))).getData();
            modelScore = qaParseBodyConstraint.getModel_score();
        }
        if(qaResult == null) {
            qaResult = new QAResult();
            qaResult.setSubject(qaParseBodyNoConstraint.getTitle());
            qaResult.setPredicate(predicate);
        }
        return new QAServiceResult(modelScore, qaResult);
    }

    public QAServiceResult haveNot(String question) throws IllegalAccessException {
        QAParseResult qaParseResult =  qaFeignService.parse(CommonUtil.entityToMutiMap(new QAParam(question))).getData();
        String predicate = "";
        double modelScore = 0.0;
        QAParseBody qaParseBodyNoConstraint = qaParseResult.getNo_constraint();
        for(QASinglePred qaSinglePred : qaParseBodyNoConstraint.getTop_k()) {
            predicate += qaSinglePred.getPred() + ",";
        }
        String prePredicate = predicate;
        QAResult qaResult = qaFeignService.batchQuery(CommonUtil.entityToMutiMap(new QAQueryParam(predicate, qaParseBodyNoConstraint.getTitle()))).getData();
        modelScore = qaParseBodyNoConstraint.getModel_score();
        if(qaResult == null) {
            predicate = "";
            QAParseBody qaParseBodyConstraint = qaParseResult.getConstraint();
            for(QASinglePred qaSinglePred : qaParseBodyConstraint.getTop_k()) {
                predicate += qaSinglePred.getPred() + ",";
            }
            qaResult = qaFeignService.batchQuery(CommonUtil.entityToMutiMap(new QAQueryParam(predicate, qaParseBodyConstraint.getTitle()))).getData();
            modelScore = qaParseBodyConstraint.getModel_score();
        }
        if(qaResult == null) {
            qaResult = new QAResult();
            qaResult.setSubject(qaParseBodyNoConstraint.getTitle());
            qaResult.setPredicate(prePredicate);
        }
        return new QAServiceResult(modelScore, qaResult);
    }
}
