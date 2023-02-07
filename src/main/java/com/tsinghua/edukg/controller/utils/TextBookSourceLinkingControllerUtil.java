package com.tsinghua.edukg.controller.utils;

import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.model.params.GetTextBookHighLightParam;
import org.apache.commons.lang3.StringUtils;

/**
 * TextBookSourceLinking Controller 参数校验
 *
 * @author tanzheng
 * @date 2022/10/12
 */
public class TextBookSourceLinkingControllerUtil {

    public static void validGetTextBookHighLightParam(GetTextBookHighLightParam param) {
        if(StringUtils.isEmpty(param.getSearchText()) || param.getPageNo() == null || param.getPageSize() == null) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
    }
}
