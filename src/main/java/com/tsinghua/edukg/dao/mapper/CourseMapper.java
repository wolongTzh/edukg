package com.tsinghua.edukg.dao.mapper;

import com.tsinghua.edukg.dao.entity.Course;

public interface CourseMapper {
    int deleteByPrimaryKey(String courseId);

    int insert(Course record);

    int insertSelective(Course record);

    Course selectByPrimaryKey(String courseId);

    Course selectByUri(String uri);

    int updateByPrimaryKeySelective(Course record);

    int updateByPrimaryKey(Course record);
}