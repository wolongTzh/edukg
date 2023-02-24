package com.tsinghua.edukg.dao.mapper;

import com.tsinghua.edukg.dao.entity.UriReCourse;

public interface UriReCourseMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UriReCourse record);

    int insertSelective(UriReCourse record);

    UriReCourse selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UriReCourse record);

    int updateByPrimaryKey(UriReCourse record);
}