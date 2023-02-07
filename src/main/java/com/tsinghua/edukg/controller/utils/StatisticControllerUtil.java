package com.tsinghua.edukg.controller.utils;

import com.tsinghua.edukg.constant.BusinessConstant;
import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.model.params.GetAllEntityListParam;
import com.tsinghua.edukg.model.params.GetEntityListParam;
import com.tsinghua.edukg.model.params.SubjectStatParam;
import org.apache.commons.lang3.StringUtils;

/**
 * statistic controller 参数校验
 *
 * @author tanzheng
 * @date 2022/10/12
 */
public class StatisticControllerUtil extends CommonControllerUtil {

    /**
     * 模糊查询接口参数校验（有分页）
     *
     * @param param
     */
    public static void validAndFillGetEntityListParam(GetEntityListParam param) {
        // 学科相关处理逻辑
        if(!StringUtils.isEmpty(param.getSubject())) {
            param.setSubject(getSubject2Label(param.getSubject()));
        }
        // 页号相关处理逻辑
        Integer pageNo = param.getPageNo();
        Integer pageSize = param.getPageSize();
        if(pageNo == null || pageSize == null) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        if(pageNo < 0) {
            throw new BusinessException(BusinessExceptionEnum.PAGE_NO_ILLEGAL);
        }
        if(pageSize > BusinessConstant.PAGE_MAX_SIZE) {
            throw new BusinessException(BusinessExceptionEnum.PAGE_SIZE_TOO_LARGE);
        }
        if(pageNo == 0) {
            param.setPageSize(BusinessConstant.PAGE_DEFAULT_SIZE);
        }
        // 页码应从0开始
        else {
            param.setPageNo(--pageNo);
        }
    }

    /**
     * 模糊查询接口参数校验（无分页）
     *
     * @param param
     */
    public static void validGetAllEntityListParam(GetAllEntityListParam param) {
        // 学科相关处理逻辑
        if(!StringUtils.isEmpty(param.getSubject())) {
            param.setSubject(getSubject2Label(param.getSubject()));
        }
    }


    /**
     * 校验更新某学科统计信息接口参数
     *
     * @param param
     */
    public static void validsubjectStatParam(SubjectStatParam param) {
        if(StringUtils.isEmpty(param.getSubject())) {
            throw new BusinessException(BusinessExceptionEnum.PARAMETER_MISSING_ERROR);
        }
        param.setSubject(getSubject2En(param.getSubject()));
    }
}
