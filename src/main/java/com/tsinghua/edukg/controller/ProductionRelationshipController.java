package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.model.CompanyEntryNode;
import com.tsinghua.edukg.model.ProductionRelationship;
import com.tsinghua.edukg.model.RelationshipDto;
import com.tsinghua.edukg.service.ProductionRelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(value = "productionRelationship")
@Slf4j
public class ProductionRelationshipController {

    @Autowired
    private ProductionRelationshipService productionRelationshipService;

    /**
     * 关联公司产品 关系
     *
     * @param startId
     * @param endId
     * @return
     */
    @GetMapping(value = "addRelationship")
    public WebResInfo addRelationship(String startId, String endId) {
        log.info("addRelationship->startId:{}，endId:{}", startId, endId);
        WebResInfo webResInfo = new WebResInfo();
        try {
            webResInfo.setCode(0);
            ProductionRelationship productionRelationship = productionRelationshipService.addProductionRelationship(startId, endId);
            webResInfo.setData(productionRelationship);

        } catch (Exception e) {
            log.error("addRelationship error:{}", e);
            webResInfo.setCode(500);
            webResInfo.setMessage(e.getMessage());
        }
        return webResInfo;
    }

    /**
     * 根据产品获取供应商信息
     *
     * @param productEntryId
     * @return
     */
    @GetMapping(value = "getCompanyByProductId")
    public WebResInfo getCompanyByProductId(String productEntryId) {
        log.info("getCompanyByProductId->productEntryId:{}", productEntryId);
        WebResInfo webResInfo = new WebResInfo();
        try {
            webResInfo.setCode(0);
            List<CompanyEntryNode> companyByProductId = productionRelationshipService.getCompanyByProductId(productEntryId);
            webResInfo.setData(companyByProductId);
        } catch (Exception e) {
            log.error("getCompanyByProductId error:{}", e);
            webResInfo.setCode(500);
            webResInfo.setMessage(e.getMessage());
        }
        return webResInfo;
    }

    /**
     * 获取联合实体信息
     *
     * @param productEntryId
     * @return
     */
    @GetMapping(value = "findRelationship")
    public WebResInfo findRelationship(String companyEntryId, String productEntryId, String relationName) {
        WebResInfo webResInfo = new WebResInfo();
        try {
            webResInfo.setCode(0);
            List<RelationshipDto> relationshipDtoList = productionRelationshipService.findRelationship(companyEntryId, productEntryId, relationName);
            webResInfo.setData(relationshipDtoList);
        } catch (Exception e) {
            log.error("findRelationship error:{}", e);
            webResInfo.setCode(500);
            webResInfo.setMessage(e.getMessage());
        }
        return webResInfo;
    }

    /**
     * 获取非联合实体信息
     *
     * @param productEntryId
     * @return
     */
    @GetMapping(value = "findProductRelationship")
    public WebResInfo findProductRelationship(String companyEntryId, String productEntryId, String relationName) {
        WebResInfo webResInfo = new WebResInfo();
        try {
            webResInfo.setCode(0);
            ProductionRelationship productionRelationship = productionRelationshipService.findProductionRelationship(companyEntryId, productEntryId, relationName);
            webResInfo.setData(productionRelationship);
        } catch (Exception e) {
            log.error("findRelationship error:{}", e);
            webResInfo.setCode(500);
            webResInfo.setMessage(e.getMessage());
        }
        return webResInfo;
    }
}
