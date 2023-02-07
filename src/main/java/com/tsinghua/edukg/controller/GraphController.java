package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.controller.utils.GraphControllerUtil;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.VO.LinkingVO;
import com.tsinghua.edukg.model.Relation;
import com.tsinghua.edukg.model.WebResInfo;
import com.tsinghua.edukg.model.params.HotEntitiesParam;
import com.tsinghua.edukg.model.params.LinkingParam;
import com.tsinghua.edukg.model.params.SearchSubgraphParam;
import com.tsinghua.edukg.service.GraphService;
import com.tsinghua.edukg.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * graph controller
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@RestController
@RequestMapping(value = "api/graph")
@Slf4j
public class GraphController {

    @Autowired
    GraphService graphService;

    /**
     * 通过uri查询实体接口
     *
     * @return
     */
    @GetMapping(value = "getInstanceInfo")
    public WebResInfo getEntity(String uri) {
        GraphControllerUtil.validGetEntityParam(uri);
        Entity entity = graphService.getEntityFromUri(uri);
        return WebUtil.successResult(entity);
    }

    /**
     * 通过实体名查询实体接口
     *
     * @return
     */
    @GetMapping(value = "findInstanceByName")
    public WebResInfo getEntityFromName(String searchText) {
        GraphControllerUtil.validGetEntityParam(searchText);
        List<Entity> entityList = graphService.getEntityFromName(searchText);
        return WebUtil.successResult(entityList);
    }


    /**
     * 获取九学科各自范围内的涉及关系数量最多的前10个实体
     *
     * @return
     */
    @GetMapping(value = "hotEntities")
    public WebResInfo hotEntities(HotEntitiesParam param) {
        GraphControllerUtil.validAndFillHotEntitiesParam(param);
        List<Entity> entities = graphService.getHotEntities(param);
        return WebUtil.successResult(entities);
    }

    /**
     * 两实体间子图查询
     *
     * @return
     */
    @PostMapping(value = "findPath")
    public WebResInfo searchSubgraph(@RequestBody SearchSubgraphParam param) {
        GraphControllerUtil.validSearchSubgraphParam(param);
        List<Relation> relations = graphService.searchSubgraph(param);
        return WebUtil.successResult(relations);
    }

    /**
     * 实体链接查询
     *
     * @return
     */
    @PostMapping(value = "instanceLinking")
    public WebResInfo linking(@RequestBody LinkingParam param) {
        GraphControllerUtil.validLinkingParam(param);
        List<LinkingVO> linkingVOList = graphService.linkingEntities(param);
        return WebUtil.successResult(linkingVOList);
    }
}
