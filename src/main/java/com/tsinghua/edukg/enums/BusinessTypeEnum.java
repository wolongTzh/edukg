package com.tsinghua.edukg.enums;

/**
 * 业务场景枚举
 *
 * @author tanzheng
 * @date 2022/12/01
 */
public enum BusinessTypeEnum {

    QA(1, "问答场景"),
    LINKING(2, "实体链接场景");

    private Integer code;

    private String msg;

    BusinessTypeEnum(Integer code, String msg) {
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
