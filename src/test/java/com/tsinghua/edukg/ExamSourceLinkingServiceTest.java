package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.enums.BusinessTypeEnum;
import com.tsinghua.edukg.model.VO.GetExamSourceVO;
import com.tsinghua.edukg.model.params.GetExamSourceParam;
import com.tsinghua.edukg.service.ExamSourceLinkingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest
@Slf4j
public class ExamSourceLinkingServiceTest {

    @Resource
    ExamSourceLinkingService examSourceLinkingService;

    @Test
    public void getExamSourceFromUri() throws IOException {
        String uri = "http://edukg.org/knowledge/3.0/instance/biology#main-E6367";
        GetExamSourceParam param = new GetExamSourceParam();
        param.setPageNo(2);
        param.setPageSize(4);
        param.setUri(uri);
        GetExamSourceVO getExamSourceVO = examSourceLinkingService.getExamSourceFromUri(param);
        log.info(JSON.toJSONString(getExamSourceVO));
    }

    @Test
    public void getExamSourceFroText() throws IOException {
        String text = "克隆";
        GetExamSourceParam param = new GetExamSourceParam();
        param.setPageNo(2);
        param.setPageSize(5);
        param.setSearchText(text);
        GetExamSourceVO getExamSourceVO = examSourceLinkingService.getExamSourceFromText(param, BusinessTypeEnum.LINKING);
        log.info(JSON.toJSONString(getExamSourceVO));
    }
}
