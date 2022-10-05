package com.tsinghua.edukg.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "ProductEntry")
@Data
public class ProductEntryNode {

    @Id
    private String productEntryId;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 词条名称
     */
    private String name;

    /**
     * 词条类型  1:产品种类 2:产品类型 3:产品单元
     */
    private String type;
    /**
     * 别名
     */
    private String aliasName;

    /**
     * 简介
     */
    private String introduction;
}
