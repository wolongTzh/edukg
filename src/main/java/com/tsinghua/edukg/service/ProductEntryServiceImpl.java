package com.tsinghua.edukg.service;

import com.google.common.collect.Lists;
import com.tsinghua.edukg.dao.neo.ProductEntryRepository;
import com.tsinghua.edukg.model.CompanyEntryNode;
import com.tsinghua.edukg.model.ProductEntryNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProductEntryServiceImpl implements ProductEntryService {

    @Autowired
    private ProductEntryRepository productEntryRepository;
    /**
     * 获取所有数据
     *
     * @return
     */
    @Override
    public List<ProductEntryNode> getAll() {
        Iterable<ProductEntryNode> all = productEntryRepository.findAll();
        List<ProductEntryNode> productEntryNodes = Lists.newArrayList(all);
        return productEntryNodes;
    }

    /**
     * 修改
     *
     * @param productEntryNode
     */
    @Override
    public void modifyCompanyEntry(ProductEntryNode productEntryNode) {
        if(StringUtils.isEmpty(productEntryNode.getProductEntryId())){
            productEntryNode.setProductEntryId(UUID.randomUUID().toString());
        }
        productEntryRepository.save(productEntryNode);
    }

    /**
     * 删除
     *
     * @param productId
     */
    @Override
    public void deleteById(String productId) {
        productEntryRepository.deleteById(productId);
    }

    /**
     * 查询
     *
     * @param productId
     * @return
     */
    @Override
    public ProductEntryNode findById(String productId) {
        Optional<ProductEntryNode> byId = productEntryRepository.findById(productId);
        if (byId.isPresent()) {
            return byId.get();
        }
        return new ProductEntryNode();
    }
}
