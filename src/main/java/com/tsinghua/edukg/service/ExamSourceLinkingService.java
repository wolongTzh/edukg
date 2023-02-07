package com.tsinghua.edukg.service;

import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.enums.BusinessTypeEnum;
import com.tsinghua.edukg.model.VO.GetExamSourceVO;
import com.tsinghua.edukg.model.params.GetExamSourceParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 试题资源读取 service
 *
 * @author tanzheng
 * @date 2022/11/3
 */
@Service
public interface ExamSourceLinkingService {

    public GetExamSourceVO getExamSourceFromUri(GetExamSourceParam param) throws IOException;

    public GetExamSourceVO getExamSourceFromText(GetExamSourceParam param, BusinessTypeEnum type) throws IOException;
}
