package com.tsinghua.edukg.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JiebaHelper {

    @Autowired
    JiebaSegmenter jiebaSegmenter;

    public List<String> cutWords(String sents) {
        List<String> retWords = new ArrayList<>();
        if(sents.contains("李白")) {
            List<String> innerSeg = Arrays.asList(sents.split("李白"));
            for(int i = 0; i < innerSeg.size(); i++) {
                retWords.addAll(jiebaSegmenter.sentenceProcess(innerSeg.get(i)));
                if(i != innerSeg.size()-1) {
                    retWords.add("李白");
                }
            }
            if(!String.join("", retWords).contains("李白")) {
                retWords.add("李白");
            }
            return retWords;
        }
        return jiebaSegmenter.sentenceProcess(sents);
    }
}
