package com.tsinghua.edukg.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "CompanyEntry")
@Data
public class CompanyEntryNode {

    @Id
    private String uuid;
    /**
     * 名称
     */
    private String name;

    /**
     * 公司表id
     */
    private String companyId;

    /**
     * 类型
     */
    private String type;

    /**
     * 别名
     */
    private String aliasName;
    /**
     * 行业
     */
    private String industry;

    /**
     * 经营范围
     */
    private String scope;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 图片路径
     */
    private String imagePath;
    /**
     * 状态 0草稿 1已审核
     */
    private String status;

    /**
     * 修改人Id
     */
    private String modifyUserId;

    /**
     * 修改时间
     */
    private Long modifyTime;

    /**
     * 创建人Id
     */
    private String createUserId;

    /**
     * 创建时间
     */
    private Long createTime;
}
