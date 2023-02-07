package com.tsinghua.edukg.config;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Configuration
public class JiebaConfig {

    String baseJiebaDict;

    List<String> subjectList = Arrays.asList("biology","chemistry","chinese","geo","history","math","physics","politics");

    @Bean
    public JiebaSegmenter segmenterGenerator(AddressConfig addressConfig) {
        if(System.getProperty("os.name").startsWith("Linux")) {
            baseJiebaDict = addressConfig.getJiebaDictAddressLinux();
        }
        else {
            baseJiebaDict = addressConfig.getJiebaDictAddressWin();
        }
        for(String subject : subjectList) {
            Path subjectPath = Paths.get(String.format(baseJiebaDict, subject));
            WordDictionary.getInstance().loadUserDict(subjectPath, Charset.forName("UTF-8"));
        }
        return new JiebaSegmenter();
    }
}
