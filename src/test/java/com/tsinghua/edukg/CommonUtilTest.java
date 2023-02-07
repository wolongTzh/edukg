package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
public class CommonUtilTest {

    @Test
    public void pageHelperTest() {
        List<Integer> input = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        int pageNum = 1;
        int pageSize = 10;
        List<Integer> output = CommonUtil.pageHelper(input, pageNum, pageSize);
        log.info(JSON.toJSONString(output));
    }

    @Test
    public void getMiddleTextFromTagsTest() {
        String source = "\uFEFF\uFEFF<html xmlns=\\\"http://www.w3.org/1999/xhtml\\\" xml:lang=\\\"zh-CN\\\">\\n\\n<head>\\n\\n<meta http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=UTF-8\\\" />\\n\\n<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"../Styles/Stylesheet.css\\\"/>\\n\\n<title>考点4　易混修辞格辨析</title>\\n\\n</head>\\n\\n<body>\\n\\n<h4><a id=\\\"Chapter_003_005_003_004_00089\\\"></a>考点4　易混修辞格辨析</h4>\\n\\n<p><span class=\\\"bold\\\">1.比喻与比拟</span></p>\\n\\n<p>比喻与比拟，都可以增强语言的形象性，但它们却是两种不同的修辞手法。";
        String target = "易混";
        String preTag = ">";
        String postTag = "<";
        String result = CommonUtil.getMiddleTextFromTags(source, target, preTag, postTag).get(0);
        log.info(result);
        preTag = "。";
        postTag = "。";
        result = CommonUtil.getMiddleTextFromTags(result, target, preTag, postTag).get(0);
        log.info(result);
    }

}
