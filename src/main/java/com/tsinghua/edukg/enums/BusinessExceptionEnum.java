package com.tsinghua.edukg.enums;

import com.tsinghua.edukg.constant.WebConstant;
import java.util.Objects;

/**
 * 业务异常枚举
 *
 * @author tanzheng
 * @date 2022/10/12
 */
public enum BusinessExceptionEnum {

    /**
     * 参数异常
     */
    USER_CHECK_ERROR(WebConstant.RESULT_FORBIDDEN_CODE, "用户校验未通过"),
    PARAMETER_ERROR(WebConstant.RESULT_PARAMETER_ILLEGAL, "参数异常"),
    PARAMETER_MISSING_ERROR(WebConstant.RESULT_PARAMETER_ILLEGAL, "参数缺失"),

    /**
     * 业务具体异常
     */
    SUBJECT_NOT_EXIST(WebConstant.SUBJECT_ERROR, "不存在该学科"),
    PAGE_NO_ILLEGAL(WebConstant.PAGE_ERROR, "页码错误"),
    PAGE_SIZE_TOO_LARGE(WebConstant.PAGE_ERROR, "页容量过大"),
    PAGE_DARA_OVERSIZE(WebConstant.PAGE_ERROR, "分页涉及数据超过总量"),
    START_OR_TAIL_URI_ERROR(WebConstant.NODE_FIND_ERROR, "起点或终点uri有误"),
    LABEL_PATTERN_ERROR(WebConstant.PATTERN_COMPILE_ERROR, "label正则表达式匹配错误"),
    ;

    public static BusinessExceptionEnum getByCode(Integer code) {
        for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
            if (Objects.equals(code, exceptionEnum.getCode())) {
                return exceptionEnum;
            }
        }
        return null;
    }

    private Integer code;

    private String msg;

    BusinessExceptionEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
