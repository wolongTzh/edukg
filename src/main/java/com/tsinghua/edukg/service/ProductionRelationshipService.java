package com.tsinghua.edukg.service;

import com.tsinghua.edukg.model.CompanyEntryNode;
import com.tsinghua.edukg.model.ProductEntryNode;
import com.tsinghua.edukg.model.ProductionRelationship;
import com.tsinghua.edukg.model.RelationshipDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductionRelationshipService {

    /**
     * 添加公司产品 关系
     *
     * @param startNode
     * @param toNode
     * @return
     */
    ProductionRelationship addProductionRelationship(CompanyEntryNode startNode, ProductEntryNode toNode);

    /**
     * 添加公司产品 关系
     *
     * @param startNodeId
     * @param toNodeId
     * @return
     */
    ProductionRelationship addProductionRelationship(String startNodeId, String toNodeId);

    /**
     * 获取产品的供应商公司
     *
     * @param productEntryId
     * @return
     */
    List<CompanyEntryNode> getCompanyByProductId(String productEntryId);

    /**
     * 自定义返回实体
     * @param companyEntryId
     * @param productEntryId
     * @param relationName
     * @return
     */
    List<RelationshipDto> findRelationship(String companyEntryId, String productEntryId, String relationName);

    /**
     * 非自定义返回实体
     * @param companyEntryId
     * @param productEntryId
     * @param relationName
     * @return
     */
    ProductionRelationship findProductionRelationship(String companyEntryId, String productEntryId, String relationName);

}
