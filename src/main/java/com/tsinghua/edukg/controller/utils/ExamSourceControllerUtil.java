package com.tsinghua.edukg.controller.utils;

import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.model.params.GetExamSourceParam;
import org.apache.commons.lang3.StringUtils;

/**
 * 试题资源读取 controller 参数校验
 *
 * @author tanzheng
 * @date 2022/11/3
 */
public class ExamSourceControllerUtil extends CommonControllerUtil {

    /**
     * 校验根据uri获取试题资源的接口参数
     *
     * @param param
     */
    public static void validGetFromUri(GetExamSourceParam param) {
        if(StringUtils.isEmpty(param.getUri()) || param.getPageNo() == null || param.getPageSize() == null) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
    }

    /**
     * 校验根据文本获取试题资源的接口参数
     *
     * @param param
     */
    public static void validGetFromText(GetExamSourceParam param) {
        if(StringUtils.isEmpty(param.getSearchText()) || param.getPageNo() == null || param.getPageSize() == null) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
    }
}
