package com.tsinghua.edukg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsinghua.edukg.dao.entity.Course;
import com.tsinghua.edukg.dao.entity.UriReCourse;
import com.tsinghua.edukg.dao.mapper.UriReCourseMapper;
import com.tsinghua.edukg.manager.sql.CourseManager;
import com.tsinghua.edukg.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
public class SqlManagerTest {

    @Autowired
    CourseManager courseManager;

    @Resource
    UriReCourseMapper uriReCourseMapper;

    @Test
    public void testGetCourseById() {
        String courseId = "1001";
        Course course = courseManager.getCourseById(courseId);
        System.out.println(JSON.toJSONString(course));
    }

    @Test
    public void testGetCourseByUri() {
        String uri = "http://edukg.org/knowledge/3.0/instance/chinese#main-E4571";
        List<Course> courseList = courseManager.getCourseByUri(uri);
        System.out.println(JSON.toJSONString(courseList));
    }

    @Test
    public void buildCourseReflectionData() throws IOException {
        JSONArray courseList = CommonUtil.readJsonOut("./courseList.json").getJSONArray("content");
        for(int i=0; i<courseList.size(); i++) {
            JSONObject courseJson = courseList.getJSONObject(i);
            Integer grade = 0;
            try {
                grade = Integer.parseInt(courseJson.getString("grade"));
            }
            catch (Exception e) {
                grade = 0;
            }
            Course course = Course.builder()
                    .bookId(courseJson.getString("bookId"))
                    .bookName(courseJson.getString("bookName"))
                    .chapterId(courseJson.getString("chapterId"))
                    .chapterName(courseJson.getString("chapterName"))
                    .courseId(courseJson.getString("courseId"))
                    .courseName(courseJson.getString("courseName"))
                    .grade(grade)
                    .subject(courseJson.getString("subject"))
                    .coverImg(courseJson.getString("cover"))
                    .build();
            courseManager.insert(course);
        }

        JSONObject relation = CommonUtil.readJsonOut("./links.json");
        for(Map.Entry entry : relation.entrySet()) {
            String uri = (String) entry.getKey();
            List<String> idList = (List<String>) entry.getValue();
            for(String id : idList) {
                UriReCourse uriReCourse = UriReCourse.builder()
                        .uri(uri)
                        .courseId(id)
                        .build();
                uriReCourseMapper.insert(uriReCourse);
            }
        }
    }
}
