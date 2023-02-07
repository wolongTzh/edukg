package com.tsinghua.edukg.service;

import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.config.AddressConfig;
import com.tsinghua.edukg.config.ElasticSearchConfig;
import com.tsinghua.edukg.enums.BusinessExceptionEnum;
import com.tsinghua.edukg.enums.BusinessTypeEnum;
import com.tsinghua.edukg.exception.BusinessException;
import com.tsinghua.edukg.manager.ESManager;
import com.tsinghua.edukg.model.ExamSource;
import com.tsinghua.edukg.model.VO.GetExamSourceVO;
import com.tsinghua.edukg.model.params.GetExamSourceParam;
import com.tsinghua.edukg.utils.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 试题资源读取 service
 *
 * @author tanzheng
 * @date 2022/11/3
 */
@Service
public class ExamSourceLinkingServiceImpl implements ExamSourceLinkingService {

    String basePath;

    String uriPath =  "static/uriOut.json";

    Map<String, String> uriMap;

    String sign;

    @Resource
    ESManager esManager;

    @Autowired
    public ExamSourceLinkingServiceImpl(AddressConfig addressConfig) {
        basePath = addressConfig.getExamSourceAddress();
        sign = addressConfig.getSign();
        uriMap = CommonUtil.readJsonInResource(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(uriPath)));
    }

    @Override
    public GetExamSourceVO getExamSourceFromUri(GetExamSourceParam param) throws IOException {
        List<ExamSource> examSourceList = new ArrayList<>();
        String uri = param.getUri();
        String name = uriMap.get(uri);
        if(StringUtils.isEmpty(name)) {
            return new GetExamSourceVO(param.getPageNo(), param.getPageSize(), 0, examSourceList);
        }
        List<String> nameList = Arrays.asList(name.split("&"));
        for(String atom : nameList) {
            ExamSource examSource = esManager.getExamSourceFromId(atom);
            if(examSource != null) {
                examSourceList.add(examSource);
            }
        }
        List<ExamSource> retList = CommonUtil.pageHelper(examSourceList, param.getPageNo() - 1, param.getPageSize());
        if(retList == null) {
            throw new BusinessException(BusinessExceptionEnum.PAGE_DARA_OVERSIZE);
        }
        GetExamSourceVO getExamSourceVO = new GetExamSourceVO(param.getPageNo(), param.getPageSize(), examSourceList.size(), retList);
        return getExamSourceVO;
    }

    @Override
    public GetExamSourceVO getExamSourceFromText(GetExamSourceParam param, BusinessTypeEnum type) throws IOException {
        String text = param.getSearchText();
        List<ExamSource> examSourceList = new ArrayList<>();
        if(type == BusinessTypeEnum.LINKING) {
            examSourceList = esManager.getExamSourceFromTerm(text);
        }
        if(type == BusinessTypeEnum.QA) {
            examSourceList = esManager.getExamSourceFromMatch(text);
        }
        if(examSourceList.size() == 0) {
            return new GetExamSourceVO(param.getPageNo(), param.getPageSize(), 0, examSourceList);
        }
        List<ExamSource> retList = CommonUtil.pageHelper(examSourceList, param.getPageNo() - 1, param.getPageSize());
        if(retList == null) {
            throw new BusinessException(BusinessExceptionEnum.PAGE_DARA_OVERSIZE);
        }
        GetExamSourceVO getExamSourceVO = new GetExamSourceVO(param.getPageNo(), param.getPageSize(), examSourceList.size(), retList);
        return getExamSourceVO;
    }

    List<JSONObject> findExamSourceJsonByNames(List<String> nameList) throws IOException {
        List<JSONObject> results = new ArrayList<>();
        for(String n : nameList) {
            n = n.replace("-", sign);
            n += ".json";
            results.add(CommonUtil.readJsonOut(basePath + sign +  n));
        }
        return results;
    }
}
