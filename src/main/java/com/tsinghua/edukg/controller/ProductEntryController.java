package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.model.ProductEntryNode;
import com.tsinghua.edukg.service.ProductEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "productEntry")
@Slf4j
public class ProductEntryController {

    @Autowired
    private ProductEntryService productEntryService;


    /**
     * 获取产品词条
     *
     * @return
     */
    @GetMapping(value = "getAll")
    public WebResInfo getAll() {
        log.info("getAll->productEntry");
        WebResInfo webResInfo = new WebResInfo();
        try {
            webResInfo.setCode(0);
            List<ProductEntryNode> all = productEntryService.getAll();
            webResInfo.setData(all);
        } catch (Exception e) {
            log.error("getAll error:{}", e);
            webResInfo.setCode(500);
            webResInfo.setMessage(e.getMessage());
        }
        return webResInfo;
    }


    /**
     * 插入或修改产品词条
     *
     * @return
     */
    @PostMapping(value = "save")
    public WebResInfo save(@RequestBody ProductEntryNode productEntryNode) {
        log.info("save->productEntryNode{}", productEntryNode);
        WebResInfo webResInfo = new WebResInfo();
        try {
            webResInfo.setCode(0);
            productEntryService.modifyCompanyEntry(productEntryNode);
        } catch (Exception e) {
            log.error("save error:{}", e);
            webResInfo.setCode(500);
            webResInfo.setMessage(e.getMessage());
        }
        return webResInfo;
    }
}
