package com.tsinghua.edukg.model;

import lombok.Data;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @Author Created by YangMeng on 2021/3/4 14:09
 * 公司->生产 产品关系
 * 指定关系名称为Production
 */
@Data
@RelationshipEntity(type = "Production")
public class ProductionRelationship {

    @Id
    private String uuid;
    @StartNode
    private CompanyEntryNode startNode;

    @EndNode
    private ProductEntryNode endNode;

    /**
     * 收入占比
     */
    private String incomeProportion;

    /**
     * 毛利率
     */
    private String productGross;

    /**
     * 产品单价
     */
    private String productPrice;

    /**
     * 产能
     */
    private String capacity;

    /**
     * 产能利用率
     */
    private String capacityRatio;

    /**
     * 产能占比
     */
    private String capacityProportion;

}
