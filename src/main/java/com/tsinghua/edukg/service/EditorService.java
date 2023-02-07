package com.tsinghua.edukg.service;

import com.tsinghua.edukg.model.DTO.QuikAddEntitiesDTO;
import com.tsinghua.edukg.model.VO.QuikAddEntitiesVO;
import com.tsinghua.edukg.model.params.GetRelationAndPropertyParam;
import com.tsinghua.edukg.model.params.UpdateLabelsParam;
import com.tsinghua.edukg.model.params.UpdatePropertyParam;
import com.tsinghua.edukg.model.params.UpdateRelationParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * editor service
 *
 * @author tanzheng
 * @date 2022/10/12
 */
@Service
public interface EditorService {

    List<String> getPropertyList(GetRelationAndPropertyParam param);

    List<String> getRelationList(GetRelationAndPropertyParam param);

    void updateProperty(UpdatePropertyParam param);

    void updateRelation(UpdateRelationParam param);

    void updateLabels(UpdateLabelsParam param);

    List<QuikAddEntitiesVO> quickAddEntities(QuikAddEntitiesDTO quikAddEntitiesDTO);
}
