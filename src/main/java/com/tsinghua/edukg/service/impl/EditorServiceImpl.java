package com.tsinghua.edukg.service.impl;

import com.tsinghua.edukg.manager.NeoManager;
import com.tsinghua.edukg.model.DTO.QuikAddEntitiesDTO;
import com.tsinghua.edukg.model.VO.QuikAddEntitiesVO;
import com.tsinghua.edukg.model.params.GetRelationAndPropertyParam;
import com.tsinghua.edukg.model.params.UpdateLabelsParam;
import com.tsinghua.edukg.model.params.UpdatePropertyParam;
import com.tsinghua.edukg.model.params.UpdateRelationParam;
import com.tsinghua.edukg.service.EditorService;
import com.tsinghua.edukg.utils.RuleHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * editor service impl
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Service
public class EditorServiceImpl implements EditorService {

    @Resource
    NeoManager neoManager;

    @Override
    public List<String> getPropertyList(GetRelationAndPropertyParam param) {
        List<String> result = RuleHandler.getSubjectProperties(param.getSubject());
        if(!StringUtils.isEmpty(param.getKeyword())) {
            result = result.stream().filter(n -> n.contains(param.getKeyword())).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<String> getRelationList(GetRelationAndPropertyParam param) {
        List<String> result = RuleHandler.getSubjectRelations(param.getSubject());
        if(!StringUtils.isEmpty(param.getKeyword())) {
            result = result.stream().filter(n -> n.contains(param.getKeyword())).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public void updateProperty(UpdatePropertyParam param) {
        neoManager.updateProperty(param.getUri(), param.getPrev(), param.getNext());
    }

    @Override
    public void updateRelation(UpdateRelationParam param) {
        neoManager.updateRelation(param.getPrev(), param.getNext());
    }

    @Override
    public void updateLabels(UpdateLabelsParam param) {
        neoManager.updateLabels(param.getUri(), param.getLabels());
    }

    @Override
    public List<QuikAddEntitiesVO> quickAddEntities(QuikAddEntitiesDTO quikAddEntitiesDTO) {
        return neoManager.batchAddEntities(quikAddEntitiesDTO.getSubject(), quikAddEntitiesDTO.getLabel(), quikAddEntitiesDTO.getEntities());
    }
}
