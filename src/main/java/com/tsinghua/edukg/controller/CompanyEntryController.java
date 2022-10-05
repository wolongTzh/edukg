package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.model.CompanyEntryNode;
import com.tsinghua.edukg.service.CompanyEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "companyEntry")
@Slf4j
public class CompanyEntryController {

    @Autowired
    private CompanyEntryService companyEntryService;

    /**
     * 获取公司词条
     *
     * @return
     */
    @GetMapping(value = "getAll")
    public WebResInfo getAll() {
        log.info("getAll->companyEntry");
        WebResInfo webResInfo = new WebResInfo();
        try {
            webResInfo.setCode(0);
            List<CompanyEntryNode> all = companyEntryService.getAll();
            webResInfo.setData(all);
        } catch (Exception e) {
            log.error("getAll error:{}", e);
            webResInfo.setCode(500);
            webResInfo.setMessage(e.getMessage());
        }
        return webResInfo;
    }

    /**
     * 插入或修改公司词条
     *
     * @return
     */
    @PostMapping(value = "save")
    public WebResInfo save(@RequestBody CompanyEntryNode companyEntryNode) {
        log.info("save->companyEntryNode{}", companyEntryNode);
        WebResInfo webResInfo = new WebResInfo();
        try {
            webResInfo.setCode(0);
            companyEntryService.modifyCompanyEntry(companyEntryNode);
        } catch (Exception e) {
            log.error("save error:{}", e);
            webResInfo.setCode(500);
            webResInfo.setMessage(e.getMessage());
        }
        return webResInfo;
    }

    /**
     * 批量插入实体测试
     *
     * @return
     */
    @PostMapping(value = "freeSave")
    public WebResInfo freeSave() {
        WebResInfo webResInfo = new WebResInfo();
        try {
            webResInfo.setCode(0);
            companyEntryService.freeSave();
        } catch (Exception e) {
            log.error("save error:{}", e);
            webResInfo.setCode(500);
            webResInfo.setMessage(e.getMessage());
        }
        return webResInfo;
    }
}
