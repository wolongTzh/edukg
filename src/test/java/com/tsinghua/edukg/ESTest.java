package com.tsinghua.edukg;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.model.TextBook;
import com.tsinghua.edukg.model.VO.GetTextBookHighLightVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
public class ESTest {

    @Resource
    ElasticsearchClient client;

    String index = "textbook";

    String id = "test";

    // 插入索引数据
    @Test
    public void insertSingle() throws IOException {
        // 创建要插入的实体
        TextBook textBook = new TextBook();
        textBook.setBookName("This is a test doc");
        textBook.setEdition("老坛");
        textBook.setEditionTime("20221109");
        // 方法二
        IndexResponse indexResponse2 = client.index(b -> b
                .index(index)
                .document(textBook)
        );
    }

    // 获取数据
    @Test
    public void grepSingle() throws IOException {
        GetResponse<TextBook> response = client.get(g -> g
                        .index(index)
                        .id(id),
                TextBook.class
        );

        if (response.found()) {
            TextBook textBook = response.source();
            log.info("返回结果 " + JSON.toJSONString(textBook));
        } else {
            log.info ("textBook not found");
        }
    }

    // 删除索引数据
    @Test
    public void deleteSingle() throws IOException {
        DeleteResponse deleteResponse = client.delete(d -> d
                .index(index)
                .id(id)
        );
    }

    // 更新索引数据
    @Test
    public void updateSingle() throws IOException {
        // 创建要更新的实体
        TextBook textBook = new TextBook();
        textBook.setBookName("老坛聊开发");
        textBook.setEdition("老坛");
        textBook.setEditionTime("20221109");
        UpdateResponse updateResponse = client.update(u -> u
                        .doc(textBook)
                        .id(id),
                TextBook.class
        );
    }

    // 高亮查询
    public void getHighLightTextBookFromText(String text) throws IOException {
        // 高亮部分前后标签
        String preHighLightTag = "<a>";
        String postHighLightTag = "</a>";
        String field = "bookName";
        List<GetTextBookHighLightVO> textBookHighLightVOList = new ArrayList<>();
        SearchResponse<TextBook> termSearch = client.search(s -> s
                        .index(index)
                        .query(q -> q
                                .term(t -> t
                                        .field(field)
                                        .value(text)
                                )
                        )
                        .highlight(h -> h
                                .fields(field, new HighlightField.Builder().build())
                                .preTags(preHighLightTag)
                                .postTags(postHighLightTag)
                        ),
                TextBook.class);
        for (Hit<TextBook> hit: termSearch.hits().hits()) {
            for (String highlight : hit.highlight().get(field)) {
                log.info(highlight);
            }
        }
    }

    @Test
    public void grepTextBook() throws IOException {
        SearchResponse<TextBook> termSearch = client.search(s -> s
                        .index(index)
                        .query(q -> q
                                .term(t -> t
                                        .field("bookName")
                                        .value("老坛")
                                )
                        ),
                TextBook.class);

        SearchResponse<TextBook> matchSearch = client.search(s -> s
                        .index(index)
                        .query(q -> q
                                .match(t -> t
                                        .field("bookName")
                                        .query("老坛")
                                        .minimumShouldMatch("100%")
                                )
                        ),
                TextBook.class);

        SearchResponse<TextBook> matchPhraseSearch = client.search(s -> s
                        .index(index)
                        .query(q -> q
                                .matchPhrase(m -> m
                                        .field("bookName")
                                        .query("老坛")
                                )
                        ),
                TextBook.class);

        SearchResponse<TextBook> multiMatchSearch = client.search(s -> s
                        .index(index)
                        .query(q -> q
                                .multiMatch(m -> m
                                        .query("老坛")
                                        .fields("edition", "bookName")
                                )
                        ),
                TextBook.class);

        SearchResponse<TextBook> fuzzySearch = client.search(s -> s
                        .index(index)
                        .query(q -> q
                                .fuzzy(f -> f
                                        .field("bookName")
                                        .fuzziness("2")
                                        .value("老坛")
                                )
                        ),
                TextBook.class);

        SearchResponse<TextBook> rangeSearch = client.search(s -> s
                        .index(index)
                        .query(q -> q
                                .range(r -> r
                                        .field("bookName")
                                        .gt(JsonData.of(20))
                                        .lt(JsonData.of(20))
                                )
                        ),
                TextBook.class);

        BoolQuery.Builder builder = new BoolQuery.Builder();
        List<String> keyWords = Arrays.asList("十月革命","意义");
        for(String key : keyWords) {
            builder.must(m -> m
                    .term(t -> t
                            .field("html")
                            .value(key)
                    )
            );
        }
        BoolQuery boolQuery = builder.build();
        SearchResponse<TextBook> boolSearch = client.search(s -> s
                        .index(index)
                        .query(q -> q
                                .bool(boolQuery

                                )
                        ),
                TextBook.class);

        for (Hit<TextBook> hit: matchSearch.hits().hits()) {
            TextBook pd = hit.source();
            System.out.println(pd);
        }
    }
}
