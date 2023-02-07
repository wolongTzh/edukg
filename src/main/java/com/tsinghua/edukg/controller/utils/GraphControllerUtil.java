package com.tsinghua.edukg.controller.utils;

import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.model.params.HotEntitiesParam;
import com.tsinghua.edukg.model.params.LinkingParam;
import com.tsinghua.edukg.model.params.SearchSubgraphParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * graph controller 参数校验
 *
 * @author tanzheng
 * @date 2022/10/12
 */
public class GraphControllerUtil extends CommonControllerUtil {

    /**
     * 通过uri查询实体接口参数校验
     *
     * @param uri
     */
    public static void validGetEntityParam(String uri) {
        if(StringUtils.isEmpty(uri)) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
    }

    /**
     * 获取九学科各自范围内的涉及关系数量最多的前10个实体参数校验
     *
     * @param param
     */
    public static void validAndFillHotEntitiesParam(HotEntitiesParam param) {
        if(StringUtils.isEmpty(param.getSubject())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        param.setSubject(getSubject2Label(param.getSubject()));
    }

    /**
     * 两实体间子图查询参数校验
     *
     * @param param
     */
    public static void validSearchSubgraphParam(SearchSubgraphParam param) {
        if(CollectionUtils.isEmpty(param.getInstanceList()) || param.getInstanceList().size() < 2) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
    }

    /**
     * 实体链接查询参数校验
     *
     * @param param
     */
    public static void validLinkingParam(LinkingParam param) {
        if(StringUtils.isEmpty(param.getSearchText())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
    }
}
