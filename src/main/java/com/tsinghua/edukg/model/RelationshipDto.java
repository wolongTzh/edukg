package com.tsinghua.edukg.model;

import lombok.Data;
import org.springframework.data.neo4j.annotation.QueryResult;

@Data
@QueryResult
public class  RelationshipDto {

    /**
     * 关系uuid
     */
    private String uuid;

    /**
     * 产品名称
     */
    private String productName;
    /**
     * 产品uuid
     */
    private String productEntryId;
    /**
     * 收入占比
     */
    private String incomeProportion;
}
