package com.tsinghua.edukg.service;

import com.tsinghua.edukg.model.CompanyEntryNode;
import com.tsinghua.edukg.model.ProductEntryNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductEntryService {

    /**
     * 获取所有数据
     *
     * @return
     */
    List<ProductEntryNode> getAll();

    /**
     * 修改
     * @param productEntryNode
     */
    void modifyCompanyEntry(ProductEntryNode productEntryNode);

    /**
     * 删除
     * @param productId
     */
    void deleteById(String productId);

    /**
     * 查询
     * @param productId
     * @return
     */
    ProductEntryNode findById(String productId);
}
