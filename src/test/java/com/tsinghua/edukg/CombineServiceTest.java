package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.model.VO.CombineLinkingVO;
import com.tsinghua.edukg.model.VO.CombineQaVO;
import com.tsinghua.edukg.model.params.TotalSearchParam;
import com.tsinghua.edukg.service.CombineService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@SpringBootTest
@Slf4j
public class CombineServiceTest {

    @Autowired
    CombineService combineService;

    @Test
    public void totalSearchTest() throws IOException {
        String searchText = "李白";
        CombineLinkingVO combineLinkingVO = combineService.totalSearch(TotalSearchParam.builder().searchText(searchText).build());
        log.info(JSON.toJSONString(combineLinkingVO));
    }

    @Test
    public void totalQaTest() throws IOException, IllegalAccessException, ExecutionException, InterruptedException {
        String searchText = "克隆技术是什么";
        CombineQaVO combineQaVO = combineService.totalQa(new QAParam(searchText));
        log.info(JSON.toJSONString(combineQaVO));
    }
}
