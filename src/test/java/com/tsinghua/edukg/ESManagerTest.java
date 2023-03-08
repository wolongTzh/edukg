package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.manager.ESManager;
import com.tsinghua.edukg.model.ExamSource;
import com.tsinghua.edukg.model.TextBook;
import com.tsinghua.edukg.model.TextBookHighLight;
import com.tsinghua.edukg.utils.HanlpHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
public class ESManagerTest {

    @Resource
    ESManager esManager;

    @Test
    public void getExamSourceFromIdTest() throws IOException {
        String id = "2017GaoKao-2017_11-11_4";
        ExamSource examSource = esManager.getExamSourceFromId(id);
        log.info(JSON.toJSONString(examSource));
    }

    @Test
    public void getExamSourceFromTextTest() throws IOException {
        String text = "克隆技术";
        List<ExamSource> examSourceList = esManager.getExamSourceFromTerm(text);
        for(ExamSource examSource : examSourceList) {
            log.info(JSON.toJSONString(examSource));
        }
    }

    @Test
    public void getExamSourceFromTextMatchTest() throws IOException {
        String text = "克隆技术";
        List<ExamSource> examSourceList = esManager.getExamSourceFromMatch(text);
        for(ExamSource examSource : examSourceList) {
            log.info(JSON.toJSONString(examSource));
        }
    }

    @Test
    public void getTextBookFromIdTest() throws IOException {
        String id = "9u14f4QB_gCAzO0aKxxt";
        TextBook textBook = esManager.getTextBookFromId(id);
        log.info(JSON.toJSONString(textBook));
    }

    @Test
    public void getTextBookFromTerm() throws IOException {
        String text = "混";
        List<TextBook> textBookList = esManager.getTextBookFromTerm(text);
        textBookList.forEach(t -> log.info(JSON.toJSONString(t)));
    }

    @Test
    public void getHighLightTextBookFromTextTest() throws IOException {
        List<String> keyWords = Arrays.asList("十月革命","意义");
        String question = "中国的首都是哪里";
        List<TextBookHighLight> retList = esManager.getHighLightTextBookFromMiniMatch(HanlpHelper.CutWordRetNeedConcernWords(question));
        for(TextBookHighLight s : retList) {
//            s.replaceAll("。.*?>","。");
            System.out.println(s.getScore() + " | " + s.getExample() + "\n\n");
        }
//        log.info(JSON.toJSONString(retList));
    }
}
