package com.tsinghua.edukg.controller.utils;

import com.tsinghua.edukg.api.model.EntityLinkParam;
import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

public class AlgorithmControllerUtil extends CommonControllerUtil {

    /**
     * 校验问答请求的接口参数
     *
     * @param param
     */
    public static void validInputQuestionParam(QAParam param) {
        if(StringUtils.isEmpty(param.getQuestion())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
    }

    /**
     * 校验实体链接的接口参数
     *
     * @param param
     */
    public static void validEntityLinkingParam(EntityLinkParam param) {
        if(StringUtils.isEmpty(param.getSubject()) || StringUtils.isEmpty(param.getText())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        getSubject2En(param.getSubject());
    }
}
