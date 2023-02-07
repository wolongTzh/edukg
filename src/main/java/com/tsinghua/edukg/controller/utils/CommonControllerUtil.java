package com.tsinghua.edukg.controller.utils;

import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.utils.RuleHandler;

/**
 * 参数校验类公共方法
 *
 * @author tanzheng
 * @date 2022/10/12
 */
public class CommonControllerUtil {


    /**
     * 将中文学科转换为学科代号
     * 注：该方法必须保证传入的学科不是空，学科为空校验请在方法外做
     *
     * @param subject 学科（非空）
     * @return
     */
    protected static String getSubject2En(String subject) {
        String enSubject = RuleHandler.convertSubjectZh2En(subject);
        if(enSubject == null) {
            throw new BusinessException(BusinessExceptionEnum.SUBJECT_NOT_EXIST);
        }
        return enSubject;
    }

    /**
     * 将中文转化为英文
     * 注：该方法必须保证传入的学科不是空，学科为空校验请在方法外做
     *
     * @param subject
     * @return
     */
    protected static String getSubject2Label(String subject) {
        return RuleHandler.convertSubject2Label(getSubject2En(subject));
    }
}
