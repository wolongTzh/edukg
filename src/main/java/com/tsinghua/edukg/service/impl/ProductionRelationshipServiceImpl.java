package com.tsinghua.edukg.service.impl;

import com.tsinghua.edukg.dao.neo.CompanyEntryRepository;
import com.tsinghua.edukg.dao.neo.ProductEntryRepository;
import com.tsinghua.edukg.dao.neo.ProductionRelationshipRepository;
import com.tsinghua.edukg.model.CompanyEntryNode;
import com.tsinghua.edukg.model.ProductEntryNode;
import com.tsinghua.edukg.model.ProductionRelationship;
import com.tsinghua.edukg.model.RelationshipDto;
import com.tsinghua.edukg.service.ProductionRelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProductionRelationshipServiceImpl implements ProductionRelationshipService {

    @Autowired
    private ProductionRelationshipRepository productionRelationshipRepository;

    @Autowired
    private CompanyEntryRepository companyEntryRepository;

    @Autowired
    private ProductEntryRepository productEntryRepository;

    /**
     * 添加公司产品 关系
     *
     * @param startNode
     * @param toNode
     * @return
     */
    @Override
    public ProductionRelationship addProductionRelationship(CompanyEntryNode startNode, ProductEntryNode toNode) {
        ProductionRelationship productionRelationship = new ProductionRelationship();
        productionRelationship.setStartNode(startNode);
        productionRelationship.setEndNode(toNode);
        //添加属性
        productionRelationship.setUuid("111111");
        ProductionRelationship save = productionRelationshipRepository.save(productionRelationship);
        return save;
    }

    /**
     * 添加公司产品 关系
     *
     * @param startNodeId
     * @param toNodeId
     * @return
     */
    @Override
    public ProductionRelationship addProductionRelationship(String startNodeId, String toNodeId) {
        Optional<CompanyEntryNode> byId = companyEntryRepository.findById(startNodeId);
        Optional<ProductEntryNode> byId1 = productEntryRepository.findById(toNodeId);
        if (byId.isPresent() && byId1.isPresent()) {
            return addProductionRelationship(byId.get(), byId1.get());
        }
        return new ProductionRelationship();
    }

    /**
     * 获取产品的供应商公司
     *
     * @param productEntryId
     * @return
     */
    @Override
    public List<CompanyEntryNode> getCompanyByProductId(String productEntryId) {
        return null;
    }

    @Override
    public List<RelationshipDto> findRelationship(String companyEntryId, String productEntryId, String relationName) {
        return productionRelationshipRepository.findRelationship(companyEntryId, productEntryId, relationName);
    }

    @Override
    public ProductionRelationship findProductionRelationship(String companyEntryId, String productEntryId, String relationName) {
        return productionRelationshipRepository.findProductionRelationship(companyEntryId, productEntryId, relationName);
    }
}
