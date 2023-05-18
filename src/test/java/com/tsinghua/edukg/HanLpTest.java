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
        String text = "孟子是儒家思想的代表人物,他的主要作品有哪些？";
        List<Term> splits = HanlpHelper.cutWords(text);
        splits = HanlpHelper.CutWordRetNeedConcernWords(text);
        for(Term term : splits) {
            System.out.println(term.word + " ");
        }
    }

    @Test
    public void jiebaSplitter() {
        List<String> retWords = jiebaHelper.cutWords("孟子是儒家思想的代表人物,他的主要作品有哪些？");
        for(String s : retWords) {
            System.out.println(s);
        }
    }
}
