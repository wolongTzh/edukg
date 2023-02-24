package com.tsinghua.edukg.manager.sql;

import com.tsinghua.edukg.dao.entity.Course;
import com.tsinghua.edukg.dao.mapper.CourseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class CourseManagerImpl implements CourseManager {

    @Resource
    CourseMapper courseMapper;

    @Override
    public Course getCourseById(String id) {
        return courseMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Course> getCourseByUri(String uri) {
        return courseMapper.selectByUri(uri);
    }
}
