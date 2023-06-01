package com.tsinghua.edukg.service.impl;

import com.tsinghua.edukg.api.feign.QAFeignService;
import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.api.model.QAResult;
import com.tsinghua.edukg.dao.entity.Course;
import com.tsinghua.edukg.enums.BusinessTypeEnum;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.EntitySimp;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.VO.*;
import com.tsinghua.edukg.model.params.GetExamSourceParam;
import com.tsinghua.edukg.model.params.GetTextBookHighLightParam;
import com.tsinghua.edukg.model.params.LinkingParam;
import com.tsinghua.edukg.model.params.TotalSearchParam;
import com.tsinghua.edukg.service.*;
import com.tsinghua.edukg.service.utils.CombineServiceUtil;
import com.tsinghua.edukg.utils.AsyncHelper;
import com.tsinghua.edukg.utils.CommonUtil;
import com.tsinghua.edukg.utils.RuleHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 综合搜索 service impl
 *
 * @author tanzheng
 * @date 2022/11/29
 */
@Service
public class CombineServiceImpl implements CombineService {

    @Resource
    NeoManager neoManager;

    @Autowired
    GraphService graphService;

    @Autowired
    QAFeignService qaFeignService;

    @Autowired
    ExamSourceLinkingService examSourceLinkingService;

    @Autowired
    TextBookLinkingService textBookLinkingService;

    @Autowired
    AsyncHelper asyncHelper;

    @Autowired
    CourseService courseService;

    Integer pageNo = 1;
    Integer pageSize = 10;

    @Override
    public CombineLinkingVO totalSearch(TotalSearchParam param) throws IOException {
        String searchText = param.getSearchText();
        // 判断是否是输入谓词的情况
        CombineLinkingVO combineLinkingVO = judgePredicate(searchText);
        if(combineLinkingVO != null) {
            combineLinkingVO.setQuestionList(examSourceLinkingService.getExamSourceFromUri(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).uri(combineLinkingVO.getInstanceInfo().getUri()).build()));
            if(CollectionUtils.isEmpty(combineLinkingVO.getQuestionList().getData())) {
                combineLinkingVO.setQuestionList(examSourceLinkingService.getExamSourceFromText(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build(), BusinessTypeEnum.LINKING));
            }
            GetTextBookHighLightVO getTextBookHighLightVO = textBookLinkingService.getHighLightMsg(GetTextBookHighLightParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build());
            combineLinkingVO.setCourseList(courseService.getCourseFromUri(combineLinkingVO.getInstanceInfo().getUri()));
            combineLinkingVO.setBookList(getTextBookHighLightVO);
            combineLinkingVO.setInstanceList(new ArrayList<>());
            return combineLinkingVO;
        }
        combineLinkingVO = new CombineLinkingVO();
        List<EntitySimp> instanceList = new ArrayList<>();
        instanceList.addAll(neoManager.getEntityListFromName(searchText));
        if(instanceList.size() > 0) {
            instanceList.subList(0, 1);
            EntitySimp entitySimp = instanceList.get(0);
            Entity entity = neoManager.getEntityFromUri(entitySimp.getUri());
            for(Relation relation : entity.getRelation()) {
                String name = "";
                String uri = "";
                if(relation.getSubject().equals(entity.getName())) {
                    name = relation.getObject();
                    uri = relation.getObjectUri();
                }
                else {
                    name = relation.getSubject();
                    uri = relation.getSubjectUri();
                }
                instanceList.add(EntitySimp.builder()
                                .name(name)
                                .uri(uri)
                        .build());
            }
        }
        if(instanceList.size() > pageSize) {
            instanceList = instanceList.subList(0, pageSize);
        }
        List<EntitySimp> finalInstanceList = new ArrayList<>();
        // 删掉不合格实体
        for(EntitySimp entitySimp : instanceList) {
            Entity entity = neoManager.getEntityFromUri(entitySimp.getUri());
            RuleHandler.propertyConverter(entity.getProperty());
            if(instanceList.size() == 1 || entity.getProperty().size() > 2) {
                Entity relationKnowledge = neoManager.getEntityFromUri(entitySimp.getUri());
                entitySimp.setClassList(relationKnowledge.getClassList());
                finalInstanceList.add(entitySimp);
            }
        }
        combineLinkingVO.setInstanceList(finalInstanceList);
        if(!CollectionUtils.isEmpty(finalInstanceList)) {
            // 首实体详细信息赋值（instanceInfo）
            Entity instanceInfo = neoManager.getEntityFromUri(finalInstanceList.get(0).getUri());
            RuleHandler.propertyConverter(instanceInfo.getProperty());
            RuleHandler.relationConverter(instanceInfo.getRelation());
            combineLinkingVO.setCourseList(courseService.getCourseFromUri(finalInstanceList.get(0).getUri()));
            combineLinkingVO.setInstanceInfo(instanceInfo);
            // 试题查询（questionList）
            combineLinkingVO.setQuestionList(examSourceLinkingService.getExamSourceFromUri(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).uri(instanceInfo.getUri()).build()));
            if(CollectionUtils.isEmpty(combineLinkingVO.getQuestionList().getData())) {
                combineLinkingVO.setQuestionList(examSourceLinkingService.getExamSourceFromText(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build(), BusinessTypeEnum.LINKING));
            }
        }
        if(finalInstanceList.size() == 0) {
            // 试题查询（questionList）
            combineLinkingVO.setQuestionList(examSourceLinkingService.getExamSourceFromText(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build(), BusinessTypeEnum.LINKING));
        }
        // 教材查询（bookList）
        GetTextBookHighLightVO getTextBookHighLightVO = textBookLinkingService.getHighLightMsg(GetTextBookHighLightParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build());
        combineLinkingVO.setBookList(getTextBookHighLightVO);
        return combineLinkingVO;
    }

    CombineLinkingVO judgePredicate(String searchText) {
        CombineLinkingVO combineLinkingVO = new CombineLinkingVO();
        String predicate = RuleHandler.getPropertyAbbrWithoutSubject(searchText);
        if(StringUtils.isEmpty(predicate)) {
            return null;
        }
        List<Entity> entityList = neoManager.getEntityListFromPredicateName(predicate);
        if(entityList.size() == 0) {
            return null;
        }
        int maxRelaIndex = 0;
        int maxRelaNum = 0;
        int cur = -1;
        for(Entity entity : entityList) {
            cur++;
            if(Math.max(entity.getRelation().size(), maxRelaNum) > maxRelaNum) {
                maxRelaNum = entity.getRelation().size();
                maxRelaIndex = cur;
            }
        }
        Entity entity = entityList.get(maxRelaIndex);
        RuleHandler.propertyConverter(entity.getProperty());
        RuleHandler.relationConverter(entity.getRelation());
        PredicateSearchVO predicateSearchVO = new PredicateSearchVO();
        for(Property property : entity.getProperty()) {
            if(property.getPredicate().equals(predicate)) {
                predicateSearchVO.setExample(property.getSubject() + " " + searchText + " " + property.getObject());
                break;
            }
        }
        predicateSearchVO.setPredicateDes(String.format("“%s”是基础教育知识图谱中的一个知识点属性", searchText));
        combineLinkingVO.setPredicateSearchVO(predicateSearchVO);
        combineLinkingVO.setInstanceInfo(entity);
        return combineLinkingVO;
    }

    @Override
    public CombineQaVO totalQa(QAParam qaParam) throws IllegalAccessException, IOException, ExecutionException, InterruptedException {
        CombineQaVO combineQaVO = new CombineQaVO();
        String searchText = qaParam.getQuestion();
        // 判断如果输入的不是个问题就返回
        if(!RuleHandler.judgeQuestion(searchText)) {
            return combineQaVO;
        }
        Future<List<QAESGrepVO>> future = asyncHelper.qaBackupForHanlp(qaParam.getQuestion());
        QAResult answer = qaFeignService.qaRequest(CommonUtil.entityToMutiMap(qaParam)).getAnswerData();
        // 没有答案的情况
        if(StringUtils.isEmpty(answer.getAnswerValue())){
            combineQaVO.setQaesGrepVO(future.get());
            return combineQaVO;
        }
        combineQaVO.setAnswer(answer);
        // 有答案没有匹配到实体的情况
        if(StringUtils.isEmpty(answer.getObjectUri())) {
            return combineQaVO;
        }
        combineQaVO.setInstanceInfo(graphService.getEntityFromUri(answer.getObjectUri()));
        // 有答案有实体，匹配资源
        combineQaVO.setQuestionList(examSourceLinkingService.getExamSourceFromUri(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).uri(answer.getObjectUri()).build()));
        if(CollectionUtils.isEmpty(combineQaVO.getQuestionList().getData())) {
            combineQaVO.setQuestionList(examSourceLinkingService.getExamSourceFromText(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build(), BusinessTypeEnum.QA));
        }
        GetTextBookHighLightVO getTextBookHighLightVO = textBookLinkingService.getHighLightMsg(GetTextBookHighLightParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build());
        combineQaVO.setBookList(getTextBookHighLightVO);
        return combineQaVO;
    }

    @Override
    public CombineQaVO simpleQA(QAParam qaParam) throws IllegalAccessException, IOException, ExecutionException, InterruptedException {
        CombineQaVO combineQaVO = new CombineQaVO();
        String searchText = qaParam.getQuestion();
        Future<List<QAESGrepVO>> future = asyncHelper.qaBackupForHanlp(qaParam.getQuestion());
        QAResult answer = qaFeignService.qaRequest(CommonUtil.entityToMutiMap(qaParam)).getAnswerData();
        // 没有答案的情况
        if(StringUtils.isEmpty(answer.getAnswerValue())){
            combineQaVO.setQaesGrepVO(future.get());
            return combineQaVO;
        }
        combineQaVO.setAnswer(answer);
        // 有答案没有匹配到实体的情况
        if(StringUtils.isEmpty(answer.getObjectUri())) {
            return combineQaVO;
        }
        combineQaVO.setInstanceInfo(graphService.getEntityFromUri(answer.getObjectUri()));
        return combineQaVO;
    }

    @Override
    public CombineQaVO totalQaForTest(QAParam qaParam) throws IllegalAccessException, IOException, ExecutionException, InterruptedException {
        CombineQaVO combineQaVO = new CombineQaVO();
        String searchText = qaParam.getQuestion();
        Future<List<QAESGrepVO>> future = asyncHelper.qaBackupForHanlpSimple(qaParam.getQuestion());
        QAResult answer = qaFeignService.qaRequest(CommonUtil.entityToMutiMap(qaParam)).getAnswerData();
        // 没有答案的情况
        if(StringUtils.isEmpty(answer.getAnswerValue())) {
            combineQaVO.setQaesGrepVO(future.get());
            return combineQaVO;
        }
        combineQaVO.setAnswer(answer);
        // 有答案没有匹配到实体的情况
        if(StringUtils.isEmpty(answer.getObjectUri())) {
            return combineQaVO;
        }
        return combineQaVO;
    }
}
