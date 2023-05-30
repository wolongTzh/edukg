package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.controller.utils.GraphControllerUtil;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.EntityWithSource;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.VO.LinkingVO;
import com.tsinghua.edukg.model.params.HotEntitiesParam;
import com.tsinghua.edukg.model.params.LinkingParam;
import com.tsinghua.edukg.model.params.SearchSubgraphParam;
import com.tsinghua.edukg.service.GraphService;
import com.tsinghua.edukg.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Slf4j
public class GraphServiceTest {

    @Resource
    GraphService graphService;

    @Test
    public void linkingEntitiesTest() {
        LinkingParam param = new LinkingParam();
        param.setSearchText("请默写古诗元日的特点元日");
        List<LinkingVO> linkingVOList = graphService.linkingEntities(param);
        log.info(JSON.toJSONString(linkingVOList));
    }

    @Test
    public void getHotEntitiesTest() {
        String subject = "语文";
        HotEntitiesParam param = new HotEntitiesParam(subject);
        GraphControllerUtil.validAndFillHotEntitiesParam(param);
        List<Entity> entityList = graphService.getHotEntities(param);
        log.info(JSON.toJSONString(entityList));
    }

    @Test
    public void getEntityWithSource() throws IOException {
        String uri = "http://edukg.org/knowledge/3.0/instance/chinese#main-E6763";
        EntityWithSource entityWithSource = graphService.getEntityWithSourceFromUri(uri);
        log.info(JSON.toJSONString(entityWithSource));
    }

    @Test
    public void findPath() {
        String subject = "http://edukg.org/knowledge/3.0/instance/chinese#main-E6763";
        String object = "http://edukg.org/knowledge/3.0/instance/chinese#main-E1525";
        SearchSubgraphParam param = new SearchSubgraphParam();
        List<String> ls = new ArrayList<>();
        ls.add(subject);
        ls.add(object);
        param.setInstanceList(ls);
        List<Relation> relations = graphService.searchSubgraph(param);
        log.info(JSON.toJSONString(relations));
    }

    @Test
    public void testHttpGet() throws IOException {
        String url = "http://47.94.201.245:8081/data/textbook/高中物理必修第三册/OEBPS/Images/Cover.jpg";
        String result = HttpUtil.sendGetData(url);
        log.info(result);
    }
}
