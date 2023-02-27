package com.tsinghua.edukg.manager.sql;

import com.tsinghua.edukg.dao.entity.Course;

import java.util.List;

public interface CourseManager {

    public Course getCourseById(String id);

    public List<Course> getCourseByUri(String uri);

    public int insert(Course course);
}
