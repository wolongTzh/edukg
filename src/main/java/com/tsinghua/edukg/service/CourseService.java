package com.tsinghua.edukg.service;

import com.tsinghua.edukg.dao.entity.Course;

import java.util.List;

public interface CourseService {

    public List<Course> getCourseFromUri(String uri);
}
