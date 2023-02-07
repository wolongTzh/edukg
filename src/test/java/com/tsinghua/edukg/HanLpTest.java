package com.tsinghua.edukg;

import com.hankcs.hanlp.seg.common.Term;
import com.tsinghua.edukg.utils.HanlpHelper;
import com.tsinghua.edukg.utils.JiebaHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
public class HanLpTest {

    @Autowired
    JiebaHelper jiebaHelper;

    @Test
    public void splitter() {
        String text = "李白字什么？";
        List<Term> splits = HanlpHelper.cutWords(text);
        String needConcernWords = HanlpHelper.CutWordRetNeedConcernWords(text);
        System.out.println(needConcernWords);
        for(Term s : splits) {
            System.out.println(s.word);
        }
    }

    @Test
    public void jiebaSplitter() {
        List<String> retWords = jiebaHelper.cutWords("你是否知道李白字什么？");
        for(String s : retWords) {
            System.out.println(s);
        }
    }
}
