package com.tsinghua.edukg.api.feign;

import com.tsinghua.edukg.api.model.ApiResult;
import com.tsinghua.edukg.api.model.qa.QAParseResult;
import com.tsinghua.edukg.api.model.qa.QAResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "qa-client", url = "${address.server.qa}")
public interface QAFeignService {

    @RequestMapping(value = "/answer", method = RequestMethod.POST)
    ApiResult<QAResult> qaRequest(@RequestBody MultiValueMap<Object, Object> param);

    @RequestMapping(value = "/parse", method = RequestMethod.POST)
    ApiResult<QAParseResult> parse(@RequestBody MultiValueMap<Object, Object> param);

    @RequestMapping(value = "/batchQuery", method = RequestMethod.POST)
    ApiResult<QAResult> batchQuery(@RequestBody MultiValueMap<Object, Object> param);
}