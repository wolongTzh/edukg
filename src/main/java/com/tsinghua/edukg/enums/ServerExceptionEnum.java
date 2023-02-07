package com.tsinghua.edukg.enums;

import com.tsinghua.edukg.constant.WebConstant;
import java.util.Objects;

/**
 * 系统异常枚举
 *
 * @author tanzheng
 * @date 2022/10/12
 */
public enum ServerExceptionEnum {

    SERVER_ERROR(WebConstant.RESULT_SERVER_ERROR_CODE, "服务器内部异常"),
    ;

    public static ServerExceptionEnum getByCode(Integer code) {
        for (ServerExceptionEnum exceptionEnum : ServerExceptionEnum.values()) {
            if (Objects.equals(code, exceptionEnum.getCode())) {
                return exceptionEnum;
            }
        }
        return null;
    }

    private Integer code;
    private String msg;

    ServerExceptionEnum(Integer code, String msg) {
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
