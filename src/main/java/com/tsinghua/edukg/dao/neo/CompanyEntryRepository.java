package com.tsinghua.edukg.dao.neo;

import com.tsinghua.edukg.model.CompanyEntryNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyEntryRepository extends Neo4jRepository<CompanyEntryNode, String> {
    /**
     * 根据公司id判断 公司是否已经存在
     *
     * @param companyId
     * @return
     */
    @Query("match(n:CompanyEntry{companyId:{companyId}}) return n.name")
    String existsByCompanyId(String companyId);

    /**
     * 记录
     * @param query
     * @return
     */
    @Query("$query")
    String record(String query);

}