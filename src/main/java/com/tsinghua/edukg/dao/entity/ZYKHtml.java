package com.tsinghua.edukg.dao.entity;

public class ZYKHtml {
    private Integer id;

    private Integer resourceId;

    private String title;

    private String fileName;

    private Integer fileSize;

    private String filePath;

    private Integer orderNum;

    private String description;

    public ZYKHtml(Integer id, Integer resourceId, String title, String fileName, Integer fileSize, String filePath, Integer orderNum, String description) {
        this.id = id;
        this.resourceId = resourceId;
        this.title = title;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.orderNum = orderNum;
        this.description = description;
    }

    public ZYKHtml() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? null : filePath.trim();
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}