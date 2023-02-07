package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.controller.utils.EditorControllerUtil;
import com.tsinghua.edukg.model.DTO.QuikAddEntitiesDTO;
import com.tsinghua.edukg.model.VO.QuikAddEntitiesVO;
import com.tsinghua.edukg.model.WebResInfo;
import com.tsinghua.edukg.model.params.*;
import com.tsinghua.edukg.service.EditorService;
import com.tsinghua.edukg.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * editor controller
 *
 * @author tanzheng
 * @date 2022/10/13
 */
@RestController
@RequestMapping(value = "api/editor")
@Slf4j
public class EditorController {

    @Autowired
    EditorService editorService;

    /**
     * 查询实体所有属性
     *
     * @return
     */
    @GetMapping(value = "getPropertyList")
    public WebResInfo getPropertyList(GetRelationAndPropertyParam param) {
        EditorControllerUtil.validGetRelationAndPropertyParam(param);
        List<String> propertyList = editorService.getPropertyList(param);
        return WebUtil.successResult(propertyList);
    }

    /**
     * 查询实体所有关系
     *
     * @return
     */
    @GetMapping(value = "getRelationList")
    public WebResInfo getRelationList(GetRelationAndPropertyParam param) {
        EditorControllerUtil.validGetRelationAndPropertyParam(param);
        List<String> relationList = editorService.getRelationList(param);
        return WebUtil.successResult(relationList);
    }


    /**
     * 更新实体属性
     *
     * @return
     */
    @PostMapping(value = "editProperty")
    public WebResInfo editProperty(@RequestBody UpdatePropertyParam param) {
        EditorControllerUtil.validUpdatePropertyParam(param);
        editorService.updateProperty(param);
        return WebUtil.successResult("");
    }

    /**
     * 更新图谱关系
     *
     * @return
     */
    @PostMapping(value = "editRelation")
    public WebResInfo editRelation(@RequestBody UpdateRelationParam param) {
        EditorControllerUtil.validUpdateRelationParam(param);
        editorService.updateRelation(param);
        return WebUtil.successResult("");
    }

    /**
     * 编辑实体概念
     *
     * @return
     */
    @PostMapping(value = "editClass")
    public WebResInfo editClass(@RequestBody UpdateLabelsParam param) {
        EditorControllerUtil.validEditClassParam(param);
        editorService.updateLabels(param);
        return WebUtil.successResult("");
    }

    /**
     * 实体快捷批量补充
     *
     * @return
     */
    @PostMapping(value = "quickAddEntities")
    public WebResInfo quickAddEntities(@RequestBody QuikAddEntitiesParam param) {
        QuikAddEntitiesDTO quikAddEntitiesDTO = EditorControllerUtil.validQuickAddEntitiesParam(param);
        List<QuikAddEntitiesVO> quikAddEntitiesVOList = editorService.quickAddEntities(quikAddEntitiesDTO);
        return WebUtil.successResult(quikAddEntitiesVOList);
    }
}
