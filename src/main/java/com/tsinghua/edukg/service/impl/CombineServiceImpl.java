package com.tsinghua.edukg.service.impl;

import com.tsinghua.edukg.api.feign.BimpmFeignService;
import com.tsinghua.edukg.api.feign.QAFeignService;
import com.tsinghua.edukg.api.model.BimpmParam;
import com.tsinghua.edukg.api.model.BimpmResult;
import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.api.model.QAResult;
import com.tsinghua.edukg.dao.entity.Course;
import com.tsinghua.edukg.enums.BusinessTypeEnum;
import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.EntitySimp;
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
    BimpmFeignService bimpmFeignService;

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
        CombineLinkingVO combineLinkingVO = new CombineLinkingVO();
        // 实体链接查询
        List<EntitySimp> instanceList = new ArrayList<>();
        List<LinkingVO> linkingEntities = graphService.linkingEntities(LinkingParam.builder().searchText(searchText).build());
        for(LinkingVO linkingVO : linkingEntities) {
            instanceList.add(CombineServiceUtil.buildEntitySimpFromLinkingVO(linkingVO));
        }
        // 根据不同情况匹配实体名称或属性（instanceList）
        if(linkingEntities.size() == 1) {
            instanceList.addAll(neoManager.getEntityWithScoreFromName(searchText));
            // 去重
            instanceList = instanceList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                    -> new TreeSet<>(Comparator.comparing(EntitySimp :: getName))), ArrayList::new));
            // 排序
            Collections.sort(instanceList, new Comparator<EntitySimp>() {
                public int compare(EntitySimp s1, EntitySimp s2) {
                    return s1.getName().length() - (s2.getName().length());
                }
            });
            // 过滤
            instanceList = instanceList.stream().filter(s -> s.getName().length() < 10).collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(linkingEntities)) {
            instanceList.addAll(neoManager.getEntityWithScoreFromName(searchText));
            instanceList.addAll(neoManager.getEntityWithScoreFromProperty(searchText));
        }
        if(instanceList.size() > pageSize) {
            instanceList = instanceList.subList(0, pageSize);
        }
        combineLinkingVO.setInstanceList(instanceList);
        if(!CollectionUtils.isEmpty(instanceList)) {
            // 首实体详细信息赋值（instanceInfo）
            Entity instanceInfo = neoManager.getEntityFromUri(instanceList.get(0).getUri());
            combineLinkingVO.setCourseList(courseService.getCourseFromUri(instanceList.get(0).getUri()));
            combineLinkingVO.setInstanceInfo(instanceInfo);
            // 试题查询（questionList）
            combineLinkingVO.setQuestionList(examSourceLinkingService.getExamSourceFromUri(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).uri(instanceInfo.getUri()).build()));
            if(CollectionUtils.isEmpty(combineLinkingVO.getQuestionList().getData())) {
                combineLinkingVO.setQuestionList(examSourceLinkingService.getExamSourceFromText(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build(), BusinessTypeEnum.LINKING));
            }
        }
        if(instanceList.size() == 0) {
            // 试题查询（questionList）
            combineLinkingVO.setQuestionList(examSourceLinkingService.getExamSourceFromText(GetExamSourceParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build(), BusinessTypeEnum.LINKING));
        }
        // 教材查询（bookList）
        GetTextBookHighLightVO getTextBookHighLightVO = textBookLinkingService.getHighLightMsg(GetTextBookHighLightParam.builder().pageNo(pageNo).pageSize(pageSize).searchText(searchText).build());
//        List<TextBook> bookList = new ArrayList<>();
//        if(getTextBookHighLightVO.getData() != null && getTextBookHighLightVO.getData().size() > 0) {
//            for(TextBookHighLight textBookHighLight : getTextBookHighLightVO.getData()) {
//                bookList.add(textBookLinkingService.getTextBookFromId(textBookHighLight.getBookId()));
//            }
//        }
//        combineLinkingVO.setBookList(TextBookVO.builder().data(bookList).pageNo(pageNo).pageSize(pageSize).totalCount(getTextBookHighLightVO.getTotalCount()).build());
        combineLinkingVO.setBookList(getTextBookHighLightVO);
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
        Future<List<QAESGrepVO>> future = asyncHelper.qaBackupForHanlpSimpleNew(qaParam.getQuestion());
        QAResult answer = qaFeignService.qaRequest(CommonUtil.entityToMutiMap(qaParam)).getAnswerData();
        List<QAESGrepVO> qaesGrepVOS = future.get();
        // kbqa不应该被选择
        if(chooseKBQA(answer, qaParam.getQuestion())) {
            combineQaVO.setAnswer(answer);
        }
        else {
//            String answers = qaesGrepVOS.get(0).getText() + "\t" + answer.getSubject() + "（的）" + answer.getPredicate() + ":" + answer.getAnswerValue();
//            BimpmParam bimpmParam = new BimpmParam(answers, qaParam.getQuestion());
//            BimpmResult bimpmResult = bimpmFeignService.bimpmRequest(bimpmParam);
//            Integer index = Integer.parseInt(bimpmResult.getIndex());
//            if(index == 0) {
//                combineQaVO.setQaesGrepVO(qaesGrepVOS);
//            }
//            else {
//                combineQaVO.setAnswer(answer);
//            }
            combineQaVO.setQaesGrepVO(qaesGrepVOS);
        }
        return combineQaVO;
    }

    public boolean chooseKBQA(QAResult answer, String question) {
        if(StringUtils.isEmpty(answer.getAnswerValue())) {
            return false;
        }
//        if(!question.contains(answer.getSubject())) {
//            return false;
//        }
//        if(answer.getScore() < 90) {
//            return false;
//        }
        return true;
    }
}
