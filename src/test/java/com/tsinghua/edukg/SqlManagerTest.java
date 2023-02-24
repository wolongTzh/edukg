package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.tsinghua.edukg.dao.entity.Course;
import com.tsinghua.edukg.manager.sql.CourseManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
public class SqlManagerTest {

    @Autowired
    CourseManager courseManager;

    @Test
    public void testGetCourseById() {
        String courseId = "1001";
        Course course = courseManager.getCourseById(courseId);
        System.out.println(JSON.toJSONString(course));
    }

    @Test
    public void testGetCourseByUri() {
        String uri = "http://edukg.org/knowledge/3.0/instance/chinese#main-E6763";
        List<Course> courseList = courseManager.getCourseByUri(uri);
        System.out.println(JSON.toJSONString(courseList));
    }

    @Test
    public void buildCourseReflectionData() {

    }
}
