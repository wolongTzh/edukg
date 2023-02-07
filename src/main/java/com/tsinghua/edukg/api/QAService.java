package com.tsinghua.edukg.api;

import com.tsinghua.edukg.api.model.QAParam;
import com.tsinghua.edukg.api.model.QAResult;
import com.tsinghua.edukg.config.AddressConfig;
import com.tsinghua.edukg.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class QAService {

    @Autowired
    RestTemplate restTemplate;

    final String inputQuestionUrl;

    @Autowired
    public QAService(AddressConfig addressConfig) {
        inputQuestionUrl = addressConfig.getQaAddress() + "course/inputQuestion";
    }

    public QAResult getQAResult(QAParam qaParam) throws IllegalAccessException {
        MultiValueMap<Object, Object> map = CommonUtil.entityToMutiMap(qaParam);
        List<QAResult> qaResult = restTemplate.postForObject(inputQuestionUrl, map, List.class);
        return CommonUtil.mapToEntity((Map)qaResult.get(0), QAResult.class);
    }
}
