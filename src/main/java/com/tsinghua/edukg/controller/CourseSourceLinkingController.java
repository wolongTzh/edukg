package com.tsinghua.edukg.controller;

import com.tsinghua.edukg.dao.entity.Course;
import com.tsinghua.edukg.service.CourseService;
import com.tsinghua.edukg.utils.WebUtil;
import com.tsinghua.edukg.model.WebResInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程资源读取 controller
 *
 * @author tanzheng
 * @date 2023/02/24
 */
@RestController
@RequestMapping(value = "api/resource")
@Slf4j
public class CourseSourceLinkingController {

    @Autowired
    CourseService courseService;

    /**
     * 通过uri查找课程资源
     *
     * @return
     */
    @GetMapping(value = "findCourse")
    public WebResInfo getBookData(String uri) {
        List<Course> courseList = courseService.getCourseFromUri(uri);
        return WebUtil.successResult(courseList);
    }
}
