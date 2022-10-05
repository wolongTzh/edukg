package com.tsinghua.edukg.service;

import com.tsinghua.edukg.model.CompanyEntryNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CompanyEntryService {

    /**
     * 获取所有数据
     *
     * @return
     */
    List<CompanyEntryNode> getAll();

    /**
     * 修改
     * @param companyEntryNode
     */
    void modifyCompanyEntry(CompanyEntryNode companyEntryNode);

    /**
     * 删除
     * @param companyId
     */
    void deleteById(String companyId);

    /**
     * 查询
     * @param companyId
     * @return
     */
    CompanyEntryNode findById(String companyId);

    void freeSave();
}
