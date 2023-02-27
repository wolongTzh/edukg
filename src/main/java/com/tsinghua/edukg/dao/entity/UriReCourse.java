package com.tsinghua.edukg.dao.entity;

import lombok.Builder;

@Builder
public class UriReCourse {
    private Integer id;

    private String uri;

    private String courseId;

    public UriReCourse(Integer id, String uri, String courseId) {
        this.id = id;
        this.uri = uri;
        this.courseId = courseId;
    }

    public UriReCourse() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri == null ? null : uri.trim();
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId == null ? null : courseId.trim();
    }
}