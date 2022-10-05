package com.tsinghua.edukg.dao.neo;

import com.tsinghua.edukg.model.ProductEntryNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductEntryRepository extends Neo4jRepository<ProductEntryNode, String> {
}