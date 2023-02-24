package com.tsinghua.edukg.dao.entity;

public class Course {
    private String courseId;

    private String courseName;

    private String coverImg;

    private Integer grade;

    private String chapterId;

    private String chapterName;

    private String bookId;

    private String bookName;

    public Course(String courseId, String courseName, String coverImg, Integer grade, String chapterId, String chapterName, String bookId, String bookName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.coverImg = coverImg;
        this.grade = grade;
        this.chapterId = chapterId;
        this.chapterName = chapterName;
        this.bookId = bookId;
        this.bookName = bookName;
    }

    public Course() {
        super();
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId == null ? null : courseId.trim();
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName == null ? null : courseName.trim();
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg == null ? null : coverImg.trim();
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId == null ? null : chapterId.trim();
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName == null ? null : chapterName.trim();
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId == null ? null : bookId.trim();
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName == null ? null : bookName.trim();
    }
}