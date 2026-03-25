package com.example.lecturemanager;

import java.io.Serializable;

public class Lecturer implements Serializable {

    String name;
    String id;
    String email;
    String phone;

   public Lecturer(){

   }
    public Lecturer(String name, String phone, String email){
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public Lecturer(String id, String name, String email, String phone) {
        this (name, phone ,email);
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
