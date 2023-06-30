package com.tsinghua.edukg.api.feign;

import com.tsinghua.edukg.api.model.BimpmParam;
import com.tsinghua.edukg.api.model.BimpmResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "bimpm-client", url = "${address.server.bimpm}")
public interface BimpmFeignService {

    @RequestMapping(value = "/answer", method = RequestMethod.POST)
    BimpmResult bimpmRequest(@RequestBody BimpmParam param);
}