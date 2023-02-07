package com.tsinghua.edukg.model.params;

import com.tsinghua.edukg.model.Entity;
import lombok.Data;

import java.util.List;

@Data
public class QuikAddEntitiesParam {

    String className;

    List<Entity> entities;
}
