package com.tsinghua.edukg.manager;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.config.AddressConfig;
import com.tsinghua.edukg.config.ElasticSearchConfig;
import com.tsinghua.edukg.model.*;
import com.tsinghua.edukg.model.VO.GetTextBookHighLightVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Component
public class ESManager {

    @Resource
    ElasticsearchClient client;

    String examSourceIndex;

    String textBookIndex;

    String irqaIndex;

    String bookPicBasePath;

    @Autowired
    public ESManager(ElasticSearchConfig elasticSearchConfig, AddressConfig addressConfig) {
        examSourceIndex = elasticSearchConfig.getExamSourceIndex();
        textBookIndex = elasticSearchConfig.getTextBookIndex();
        irqaIndex = elasticSearchConfig.getIrqaIndex();
        bookPicBasePath = addressConfig.getBookBasePath();
    }

    public TextBook getTextBookFromId(String id) throws IOException {
        GetResponse<TextBook> response = client.get(g -> g
                        .index(textBookIndex)
                        .id(id),
                TextBook.class
        );
        if (response.found()) {
            TextBook textBook = response.source();
            return textBook;
        } else {
            return null;
        }
    }

    public List<TextBook> getTextBookFromTerm(String termText) throws IOException {
        List<TextBook> textBookList = new ArrayList<>();
        SearchResponse<TextBook> termSearch = client.search(s -> s
                        .index(textBookIndex)
                        .query(q -> q
                                .term(t -> t
                                        .field("html")
                                        .value(termText)
                                )
                        ),
                TextBook.class);
        for (Hit<TextBook> hit: termSearch.hits().hits()) {
            textBookList.add(hit.source());
        }
        return textBookList;
    }

    public List<TextBook> getTextBookHighLightMsgFromTerm(String termText) throws IOException {
        List<TextBook> textBookRetList = new ArrayList<>();
        SearchResponse<TextBook> termSearch = client.search(s -> s
                        .index(textBookIndex)
                        .query(q -> q
                                .term(t -> t
                                        .field("html")
                                        .value(termText)
                                )
                        ),
                TextBook.class);
        Map<String,Integer> recordMap = new HashMap<>();
        for (Hit<TextBook> hit: termSearch.hits().hits()) {
            TextBook textBook = hit.source();
            if(recordMap.containsKey(textBook.getIsbn())) {
                TextBook target = textBookRetList.get(recordMap.get(textBook.getIsbn()));
                target.getChapterList().add(TextBookHighLight.builder()
                        .bookId(hit.id())
                        .example(hit.source().getHtml()).build());
            }
            else {
                TextBook target = new TextBook();
                BeanUtils.copyProperties(textBook, target);
                List<TextBookHighLight> chapterList = new ArrayList<>();
                chapterList.add(TextBookHighLight.builder()
                        .bookId(hit.id())
                        .example(hit.source().getHtml()).build());
                target.setChapterList(chapterList);
                target.setPicBasePath(String.format(bookPicBasePath, target.getBookName()));
                target.setHtml(null);
                target.setHtmlName(null);
                textBookRetList.add(target);
                recordMap.put(target.getIsbn(), textBookRetList.size()-1);
            }
        }
        return textBookRetList;
    }

    public List<TextBook> getTextBookHighLightMsgFromMatch(String matchText) throws IOException {
        List<TextBook> textBookRetList = new ArrayList<>();
        SearchResponse<TextBook> termSearch = client.search(s -> s
                        .index(textBookIndex)
                        .query(q -> q
                                .match(t -> t
                                        .field("html")
                                        .query(matchText)
                                )
                        ),
                TextBook.class);
        Map<String,Integer> recordMap = new HashMap<>();
        for (Hit<TextBook> hit: termSearch.hits().hits()) {
            TextBook textBook = hit.source();
            if(recordMap.containsKey(textBook.getIsbn())) {
                TextBook target = textBookRetList.get(recordMap.get(textBook.getIsbn()));
                target.getChapterList().add(TextBookHighLight.builder()
                        .bookId(hit.id())
                        .example(hit.source().getHtml()).build());
            }
            else {
                TextBook target = new TextBook();
                BeanUtils.copyProperties(textBook, target);
                List<TextBookHighLight> chapterList = new ArrayList<>();
                chapterList.add(TextBookHighLight.builder()
                        .bookId(hit.id())
                        .example(hit.source().getHtml()).build());
                target.setChapterList(chapterList);
                target.setPicBasePath(String.format(bookPicBasePath, target.getBookName()));
                target.setHtml(null);
                target.setHtmlName(null);
                textBookRetList.add(target);
                recordMap.put(target.getIsbn(), textBookRetList.size()-1);
            }
        }
        return textBookRetList;
    }

    public ExamSource getExamSourceFromId(String id) throws IOException {
        GetResponse<ExamSourceFromES> response = client.get(g -> g
                        .index(examSourceIndex)
                        .id(id),
                ExamSourceFromES.class
        );
        if (response.found()) {
            ExamSourceFromES examSourceFromES = response.source();
            String content = examSourceFromES.getContent();
            JSONObject jsonObject = JSON.parseObject(content);
            return new ExamSource(id, examSourceFromES.getSearchText(), jsonObject);
        } else {
            return null;
        }
    }

    public List<ExamSource> getExamSourceFromTerm(String termText) throws IOException {
        SearchResponse<ExamSourceFromES> termSearch = client.search(s -> s
                        .index(examSourceIndex)
                        .query(q -> q
                                .term(t -> t
                                        .field("searchText")
                                        .value(termText)
                                )
                        ),
                ExamSourceFromES.class);
        List<ExamSource> examSourceList = new ArrayList<>();
        for (Hit<ExamSourceFromES> hit: termSearch.hits().hits()) {
            String id = hit.id();
            ExamSourceFromES examSourceFromES = hit.source();
            String content = examSourceFromES.getContent();
            if(StringUtils.isEmpty(content)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(content);
            examSourceList.add(new ExamSource(id, examSourceFromES.getSearchText(), jsonObject));
        }
        return examSourceList;
    }

    /**
     * 谭峥版本irqa检索（效果不如刘阳版本）
     * @param keyWords
     * @return
     * @throws IOException
     */
    public List<TextBookHighLight> getHighLightTextBookFromText(List<String> keyWords) throws IOException {
        String field = "all";
        List<String> retList = new ArrayList<>();
        List<TextBookHighLight> textBookHighLightList = new ArrayList<>();
        BoolQuery.Builder builder = new BoolQuery.Builder();
        for(String key : keyWords) {
            builder.must(m -> m
                    .term(t -> t
                            .field(field)
                            .value(key)
                    )
            );
        }
        BoolQuery boolQuery = builder.build();
        SearchResponse<TextBook> termSearch = client.search(s -> s
                        .index(irqaIndex)
                        .query(q -> q
                                .bool(boolQuery)
                        )
                        .highlight(h -> h
                                .fields(field, new HighlightField.Builder().build())
                        ),
                TextBook.class);
        for (Hit<TextBook> hit: termSearch.hits().hits()) {
            String highlightText = "";
            for (String highlight : hit.highlight().get(field)) {
                highlightText += highlight;
            }
            textBookHighLightList.add(TextBookHighLight.builder()
                    .bookId(hit.id())
                    .example(highlightText.replaceAll("<.*?>|\n","").replaceAll("。.*?>",""))
                    .build());
        }
        return textBookHighLightList;
    }

    /**
     * 刘阳版本irqa检索
     * @param keyWords
     * @return
     * @throws IOException
     */
    public List<TextBookHighLight> getHighLightTextBookFromMiniMatch(String keyWords) throws IOException {
        String miniMatch = "";
        String field = "all";
        List<TextBookHighLight> textBookHighLightList = new ArrayList<>();
        SearchResponse<IrqaTextBook> matchSearch;
        if(keyWords.split(" ").length > 8) {
            matchSearch = client.search(s -> s
                            .index(irqaIndex)
                            .query(q -> q
                                    .match(t -> t
                                            .field(field)
                                            .query(keyWords)
                                            .minimumShouldMatch("6")
                                    )
                            )
                            .highlight(h -> h
                                    .fields(field, new HighlightField.Builder().build())
                            ),
                    IrqaTextBook.class);
        }
        else if(keyWords.split(" ").length > 3) {
            matchSearch = client.search(s -> s
                            .index(irqaIndex)
                            .query(q -> q
                                    .match(t -> t
                                            .field(field)
                                            .query(keyWords)
                                            .minimumShouldMatch("80%")
                                    )
                            )
                            .highlight(h -> h
                                    .fields(field, new HighlightField.Builder().build())
                            ),
                    IrqaTextBook.class);
        }
        else {
            matchSearch = client.search(s -> s
                            .index(irqaIndex)
                            .query(q -> q
                                    .match(t -> t
                                            .field(field)
                                            .query(keyWords)
                                            .minimumShouldMatch("100%")
                                    )
                            )
                            .highlight(h -> h
                                    .fields(field, new HighlightField.Builder().build())
                            ),
                    IrqaTextBook.class);
        }
        for (Hit<IrqaTextBook> hit: matchSearch.hits().hits()) {
            textBookHighLightList.add(TextBookHighLight.builder()
                    .bookId(hit.id())
                    .example(hit.source().getContent())
                    .score(hit.score())
                    .build());
        }
        return textBookHighLightList;
    }

    public List<ExamSource> getExamSourceFromMatch(String matchText) throws IOException {
        SearchResponse<ExamSourceFromES> matchSearch = client.search(s -> s
                        .index(examSourceIndex)
                        .query(q -> q
                                .match(t -> t
                                        .field("searchText")
                                        .query(matchText)
                                )
                        ),
                ExamSourceFromES.class);
        List<ExamSource> examSourceList = new ArrayList<>();
        for (Hit<ExamSourceFromES> hit: matchSearch.hits().hits()) {
            String id = hit.id();
            ExamSourceFromES examSourceFromES = hit.source();
            String content = examSourceFromES.getContent();
            if(StringUtils.isEmpty(content)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(content);
            examSourceList.add(new ExamSource(id, examSourceFromES.getSearchText(), jsonObject));
        }
        return examSourceList;
    }
}
