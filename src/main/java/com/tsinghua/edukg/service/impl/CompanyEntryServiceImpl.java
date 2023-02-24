package com.tsinghua.edukg.service.impl;

import com.google.common.collect.Lists;
import com.tsinghua.edukg.dao.neo.CompanyEntryRepository;
import com.tsinghua.edukg.model.CompanyEntryNode;
import com.tsinghua.edukg.service.CompanyEntryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CompanyEntryServiceImpl implements CompanyEntryService {

    @Autowired
    private CompanyEntryRepository companyEntryRepository;

    @Autowired
    @Qualifier("neo4jSession")
    Session session;

    /**
     * 获取所有数据
     *
     * @return
     */
    @Override
    public List<CompanyEntryNode> getAll() {
        Iterable<CompanyEntryNode> all = companyEntryRepository.findAll();
        List<CompanyEntryNode> companyEntryNodes = Lists.newArrayList(all);
        return companyEntryNodes;
    }

    /**
     * 修改
     *
     * @param companyEntryNode
     */
    @Override
    public void modifyCompanyEntry(CompanyEntryNode companyEntryNode) {
        if(StringUtils.isEmpty(companyEntryNode.getUuid())){
            companyEntryNode.setUuid(UUID.randomUUID().toString());
        }
        List<CompanyEntryNode> ls = new ArrayList<>();
        ls.add(companyEntryNode);
        companyEntryRepository.save(ls, 1);
    }

    /**
     * 删除
     *
     * @param companyId
     */
    @Override
    public void deleteById(String companyId) {
        companyEntryRepository.deleteById(companyId);
    }

    /**
     * 查询
     *
     * @param companyId
     * @return
     */
    @Override
    public CompanyEntryNode findById(String companyId) {
        Optional<CompanyEntryNode> byId = companyEntryRepository.findById(companyId);
        if (byId.isPresent()) {
            return byId.get();
        }
        return new CompanyEntryNode();
    }

    @Override
    public void freeSave() {
        String query = "match(n:CompanyEntry{uuid:$uuid}) return n.name as name, n.type as type";
        List<String> labels = new ArrayList<>();
        labels.add("label1");
        labels.add("label2");
        labels.add("label3");
        List<Map<String, String>> ls = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();
        properties.put("name", "tz");
        properties.put("sex", "mail");
        ls.add(properties);
        Map<String, String> properties2 = new HashMap<>();
        properties2.put("company", "tsinghua");
        properties2.put("type", "edu");
        ls.add(properties2);
        List<String> tempList = new ArrayList<>();
        for(int i=0; i<ls.size(); i++) {
            Map<String, String> map = ls.get(i);
            //String temp = "(n" + i + ":" + String.join(":", labels) + "{";
            String temp = String.format("(n%d:%s{", i, String.join(":", labels));
            for(Map.Entry entry : map.entrySet()) {
              //  temp += entry.getKey()  + ":'" + entry.getValue() + "',";
                temp += String.format("%s:'%s',", entry.getKey(), entry.getValue());
            }
            temp = temp.substring(0, temp.length() - 1) + "})";
            tempList.add(temp);
        }
        String query2 = "create" + String.join(",", tempList);
        Map<String, String> params = new HashMap<>();
//        params.put("name", "qian");
//        params.put("height", "180");
        params.put("uuid", "2e913fb3-7d86-4543-9c2e-f701f3fc0c4d");
        query2 = "create(a:Person:man{name:$name,title:'developer'}),(b:Person:woman{height:$height,grade:'middle'})";
        Result result = session.query(query, params);
        for (Map<String, Object> map : result.queryResults()) {
            for(Map.Entry<String, Object> entry : map.entrySet()) {
                String name = (String) entry.getValue();
                System.out.println(name);
            }
        }
//        String query = "match(n:CompanyEntry{companyId:'2e913fb3-7d86-4543-9c2e-f701f3fc0c4d'}) return n.name";
//        String result = companyEntryRepository.record(query);
//        System.out.println(result);
    }
}
