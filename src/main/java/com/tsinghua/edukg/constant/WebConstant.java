package com.tsinghua.edukg.constant;

/**
 * 网络请求常量（code & msg）
 *
 * @author tanzheng
 * @date 2022/10/12
 */
public class WebConstant {

    /**
     * 通用错误
     */
    public static final String RESULT_OK = "success";
    public static final int RESULT_OK_CODE = 0;//OK code
    public static final int RESULT_FORBIDDEN_CODE = 403; //Forbidden    //权限错误
    public static final int RESULT_NOFOUND_ERROR_CODE = 404;//not found error    //未找到错误
    public static final int RESULT_SERVER_ERROR_CODE = 500;//Internal Server Error //服务器内部错误
    public static final int RESULT_PARAMETER_ILLEGAL = 1003;//参数非法

    /**
     * 业务相关错误
     */
    public static final int SUBJECT_ERROR = 2001;//学科相关错误
    public static final int PAGE_ERROR = 2002;//分页相关错误
    public static final int NODE_FIND_ERROR = 2003;//图节点查询相关错误
    public static final int PATTERN_COMPILE_ERROR = 2004;//正则表达式解析相关错误
    public static final int CUSTOMIZE_ERROR = 2022;//自定义错误
}
