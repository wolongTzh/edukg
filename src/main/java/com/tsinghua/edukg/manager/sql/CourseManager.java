package com.tsinghua.edukg.manager.sql;

import com.tsinghua.edukg.dao.entity.Course;

public interface CourseManager {

    public Course getCourseById(String id);

    public Course getCourseByUri(String uri);
}
