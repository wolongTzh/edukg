package com.tsinghua.edukg.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Entity {

    String name;

    String uri;

    List<String> classNameList;

    Map<String, String> propertyMap;

    public Entity(String name, String uri, List<String> classNameList, Map<String, String> propertyMap) {
        this.name = name;
        this.uri = uri;
        this.classNameList = classNameList;
        this.propertyMap = propertyMap;
    }

    public Entity() {
    }
}
