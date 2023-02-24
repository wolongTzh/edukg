package com.tsinghua.edukg.service.impl;

import com.tsinghua.edukg.dao.entity.Course;
import com.tsinghua.edukg.manager.sql.CourseManager;
import com.tsinghua.edukg.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CourseServiceImpl implements CourseService {

    @Autowired
    CourseManager courseManager;

    @Override
    public List<Course> getCourseFromUri(String uri) {
        return courseManager.getCourseByUri(uri);
    }
}
