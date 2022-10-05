package com.tsinghua.edukg.dao.neo;


import com.tsinghua.edukg.model.CompanyEntryNode;
import com.tsinghua.edukg.model.ProductionRelationship;
import com.tsinghua.edukg.model.RelationshipDto;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionRelationshipRepository extends Neo4jRepository<ProductionRelationship, String> {

    /**
            * 通过公司词条id和产品词条id 查询 ProductionRelationship
     *
             * @param companyEntryId 公司词条id
     * @param productEntryId 产品词条id
     * @param relationName   关系类型名称
     * @return ProductionRelationship
     */
    @Query("MATCH (c:CompanyEntry)-[r]->(p:ProductEntry) " +
            "where type(r) = {relationName} " +
            "and c.uuid={companyEntryId} " +
            "and p.productEntryId = {productEntryId}" +
            "return r.uuid as uuid,p.name as productName,p.productEntryId as productEntryId")
    List<RelationshipDto> findRelationship(String companyEntryId, String productEntryId, String relationName);

     /**
             * 通过公司词条id和产品词条id 查询 ProductionRelationship
     *
             * @param companyEntryId 公司词条id
     * @param productEntryId 产品词条id
     * @param relationName   关系类型名称
     * @return ProductionRelationship
     */
    @Query("MATCH productionRelationship = (c:CompanyEntry)-[r]->(p:ProductEntry) " +
            "where type(r) = {relationName} " +
            "and c.uuid={companyEntryId} " +
            "and p.productEntryId = {productEntryId}" +
            "return productionRelationship ")
    ProductionRelationship findProductionRelationship(String companyEntryId, String productEntryId, String relationName);

}
