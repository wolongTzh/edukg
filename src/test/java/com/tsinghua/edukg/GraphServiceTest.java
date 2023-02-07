package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.controller.utils.GraphControllerUtil;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.VO.LinkingVO;
import com.tsinghua.edukg.model.params.HotEntitiesParam;
import com.tsinghua.edukg.model.params.LinkingParam;
import com.tsinghua.edukg.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
@Slf4j
public class GraphServiceTest {

    @Resource
    GraphService graphService;

    @Test
    public void linkingEntitiesTest() {
        LinkingParam param = new LinkingParam();
        param.setSearchText("李白字什么？");
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
}
