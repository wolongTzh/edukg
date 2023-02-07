package com.tsinghua.edukg.controller.utils;

import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.model.Entity;
import com.tsinghua.edukg.model.Property;
import com.tsinghua.edukg.model.DTO.QuikAddEntitiesDTO;
import com.tsinghua.edukg.model.params.*;
import com.tsinghua.edukg.utils.RuleHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.regex.Matcher;

/**
 * editor controller 参数校验
 *
 * @author tanzheng
 * @date 2022/10/12
 */
public class EditorControllerUtil extends CommonControllerUtil {

    /**
     * 校验请求实体关系属性的接口参数
     *
     * @param param
     */
    public static void validGetRelationAndPropertyParam(GetRelationAndPropertyParam param) {
        if(StringUtils.isEmpty(param.getSubject())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        param.setSubject(getSubject2En(param.getSubject()));
    }

    /**
     * 校验编辑实体属性的接口参数
     *
     * @param param
     */
    public static void validUpdatePropertyParam(UpdatePropertyParam param) {
        if(StringUtils.isEmpty(param.getUri())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        if(param.getPrev() == null || param.getNext() == null) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        if(StringUtils.isEmpty(param.getPrev().getSubject()) && StringUtils.isEmpty(param.getNext().getObject())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
    }

    /**
     * 校验编辑图谱关系的接口参数
     *
     * @param param
     */
    public static void validUpdateRelationParam(UpdateRelationParam param) {
        if(param.getPrev() == null || param.getNext() == null) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        if(StringUtils.isEmpty(param.getPrev().getPredicate()) && StringUtils.isEmpty(param.getNext().getPredicate())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        if(!StringUtils.isEmpty(param.getPrev().getPredicate())) {
            if(StringUtils.isEmpty(param.getPrev().getSubjectUri()) || StringUtils.isEmpty(param.getPrev().getObjectUri())) {
                throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
            }
        }
        if(!StringUtils.isEmpty(param.getNext().getPredicate())) {
            if(StringUtils.isEmpty(param.getNext().getSubjectUri()) || StringUtils.isEmpty(param.getNext().getSubjectUri())) {
                throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
            }
        }
    }

    /**
     * 校验编辑实体概念的接口参数
     *
     * @param param
     */
    public static void validEditClassParam(UpdateLabelsParam param) {
        if(StringUtils.isEmpty(param.getUri()) || CollectionUtils.isEmpty(param.getLabels())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
    }

    /**
     * 校验实体快捷批量补充的接口参数
     *
     * @param param
     */
    public static QuikAddEntitiesDTO validQuickAddEntitiesParam(QuikAddEntitiesParam param) {
        QuikAddEntitiesDTO quikAddEntitiesDTO = new QuikAddEntitiesDTO();
        String label = param.getClassName();
        if(StringUtils.isEmpty(label) || CollectionUtils.isEmpty(param.getEntities())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        for(Entity entity : param.getEntities()) {
            if(StringUtils.isEmpty(entity.getName())) {
                throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
            }
            if(!CollectionUtils.isEmpty(entity.getProperty())) {
                for(Property property : entity.getProperty()) {
                    if(StringUtils.isEmpty(property.getSubject()) || StringUtils.isEmpty(property.getObject())) {
                        throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
                    }
                }
            }
        }
        Matcher matcher = RuleHandler.findLabelSubject(label);
        if(!matcher.matches()) {
            throw new BusinessException(BusinessExceptionEnum.LABEL_PATTERN_ERROR);
        }
        quikAddEntitiesDTO.setSubject(matcher.group(1));
        quikAddEntitiesDTO.setLabel(param.getClassName());
        quikAddEntitiesDTO.setEntities(param.getEntities());
        return quikAddEntitiesDTO;
    }
}
