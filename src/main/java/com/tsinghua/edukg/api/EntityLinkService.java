package com.tsinghua.edukg.api;

import com.tsinghua.edukg.api.model.EntityLinkParam;
import com.tsinghua.edukg.api.model.EntityLinkResult;
import com.tsinghua.edukg.config.AddressConfig;
import com.tsinghua.edukg.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class EntityLinkService {

    @Autowired
    RestTemplate restTemplate;

    String linkingUrl;

    @Autowired
    public EntityLinkService(AddressConfig addressConfig) {
        linkingUrl = addressConfig.getLinkingAddress() + "parser/linking";
    }

    public EntityLinkResult getEntityLinkResult(EntityLinkParam param) throws IllegalAccessException {
        MultiValueMap<Object, Object> map = CommonUtil.entityToMutiMap(param);
//        List<EntityLinkResult> entityLinkResults = restTemplate.postForObject(url, map, List.class);
        String entityLinkResults = restTemplate.postForObject(linkingUrl, map, String.class);
        System.out.println(entityLinkResults);
//        return CommonUtil.mapToEntity((Map)entityLinkResults.get(0), EntityLinkResult.class);
        return null;
    }
}
