package com.example.lecturemanager;

import java.util.ArrayList;

public class Group {

    String id;
    String name;
    String description;
    ArrayList<String> lectures;





    public Group(){
    }

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
        this.lectures = new ArrayList<>();

    }

    public Group(String id, String name, String description) {
        this(name, description);
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public void setName(String Name) {
        this.name = Name;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getLectures() {
        return lectures;
    }
    public void setLectures(ArrayList<String> lectures) {
        this.lectures = lectures;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

}
