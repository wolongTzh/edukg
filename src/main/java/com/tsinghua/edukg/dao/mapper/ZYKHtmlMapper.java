package com.tsinghua.edukg.dao.mapper;

import com.tsinghua.edukg.dao.entity.ZYKHtml;

public interface ZYKHtmlMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ZYKHtml record);

    int insertSelective(ZYKHtml record);

    ZYKHtml selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ZYKHtml record);

    int updateByPrimaryKey(ZYKHtml record);
}