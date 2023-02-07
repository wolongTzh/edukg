package com.tsinghua.edukg.api.feign;

import com.tsinghua.edukg.api.model.ApiResult;
import com.tsinghua.edukg.api.model.QAResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "qa-client", url = "${address.server.qa}")
public interface QAFeignService {

    @RequestMapping(value = "/answer", method = RequestMethod.POST)
    ApiResult<QAResult> qaRequest(@RequestBody MultiValueMap<Object, Object> param);
}