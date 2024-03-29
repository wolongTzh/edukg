package com.tsinghua.edukg.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.tsinghua.edukg.config.AddressConfig;
import com.tsinghua.edukg.manager.ESManager;
import com.tsinghua.edukg.model.TextBookHighLight;
import com.tsinghua.edukg.model.VO.LinkingVO;
import com.tsinghua.edukg.model.VO.QAESGrepVO;
import com.tsinghua.edukg.model.params.LinkingParam;
import com.tsinghua.edukg.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

@Service
public class AsyncHelper {

    @Autowired
    private JiebaSegmenter segmenter;

    @Autowired
    GraphService graphService;

    @Autowired
    ESManager esManager;

    Set<String> stopWordsSet;

    public AsyncHelper(AddressConfig addressConfig) throws IOException {
        List<String> stopWordsList = CommonUtil.readTextInResource(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(addressConfig.getStopWords())));
        stopWordsSet = new HashSet<>();
        stopWordsSet.addAll(stopWordsList);
    }

    @Async(value = "esTaskThreadPool")
    public Future<List<QAESGrepVO>> qaBackup(String question) throws IOException {
        List<String> segResult = segmenter.sentenceProcess(question);
        List<QAESGrepVO> qaesGrepVOList = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        for(String word : segResult) {
            if(!stopWordsSet.contains(word)) {
                keywords.add(word);
            }
        }
        List<TextBookHighLight> sents = esManager.getHighLightTextBookFromText(keywords);
        int count = 3;
        for(TextBookHighLight sent : sents) {
            if(count == 0) {
                break;
            }
            count--;
            List<LinkingVO> linkingVOList = graphService.linkingEntities(LinkingParam.builder().searchText(sent.getExample()).build());
            qaesGrepVOList.add(QAESGrepVO.builder()
                    .bookId(sent.getBookId())
                    .linkingVOList(linkingVOList)
                    .text(sent.getExample())
                    .build());
        }
        return new AsyncResult<>(qaesGrepVOList);
    }

    @Async(value = "esTaskThreadPool")
    public Future<List<QAESGrepVO>> qaBackupForHanlp(String question) throws IOException {
        List<QAESGrepVO> qaesGrepVOList = new ArrayList<>();
        List<TextBookHighLight> sents = esManager.getHighLightTextBookFromMiniMatch(HanlpHelper.CutWordRetNeedConcernWords(question));
        int count = 3;
        for(TextBookHighLight sent : sents) {
            if(count == 0) {
                break;
            }
            count--;
            List<LinkingVO> linkingVOList = graphService.linkingEntities(LinkingParam.builder().searchText(sent.getExample()).build());
            qaesGrepVOList.add(QAESGrepVO.builder()
                    .bookId(sent.getBookId())
                    .linkingVOList(linkingVOList)
                    .text(sent.getExample())
                    .build());
        }
        return new AsyncResult<>(qaesGrepVOList);
    }

    @Async(value = "esTaskThreadPool")
    public Future<List<QAESGrepVO>> qaBackupForHanlpSimple(String question) throws IOException {
        List<QAESGrepVO> qaesGrepVOList = new ArrayList<>();
        List<TextBookHighLight> sents = esManager.getHighLightTextBookFromMiniMatch(HanlpHelper.CutWordRetNeedConcernWords(question));
        int count = 3;
        for(TextBookHighLight sent : sents) {
            if(count == 0) {
                break;
            }
            count--;
            qaesGrepVOList.add(QAESGrepVO.builder()
                    .bookId(sent.getBookId())
                    .text(sent.getExample())
                    .build());
        }
        return new AsyncResult<>(qaesGrepVOList);
    }
}
