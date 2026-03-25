package com.example.lecturemanager;

import java.util.Date;

public class Lecture {

    private String lectureId;
    private String title;
    private Date date;
    private String groupId;
    private String lecturerId;

    public Lecture() {
    }

    public Lecture(String title, String groupId, String lecturerId, String lecturerName) {
        this.title = title;
        this.date = date;
        this.groupId = groupId;
        this.lecturerId = lecturerId;


    }

    public String getLectureId() {
        return lectureId;
    }

    public void setLectureId(String lectureId) {
        this.lectureId = lectureId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }
}

