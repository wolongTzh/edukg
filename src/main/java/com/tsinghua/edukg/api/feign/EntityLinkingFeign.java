package com.tsinghua.edukg.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "linking-client", url = "http://47.94.201.245:8000")
public interface EntityLinkingFeign {

    @RequestMapping(value = "/parser/linking", method = RequestMethod.POST)
    String getEntityLinkResult(MultiValueMap<Object, Object> param);
}
