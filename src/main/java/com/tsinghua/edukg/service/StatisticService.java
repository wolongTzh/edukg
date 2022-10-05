package com.tsinghua.edukg.service;

import com.tsinghua.edukg.model.GetEntityListParam;
import com.tsinghua.edukg.model.Entity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StatisticService {

    List<Entity> getEntityList(GetEntityListParam param);
}
