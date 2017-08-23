package org.eyeseetea.malariacare.domain.entity;

public class Program {
    String name;
    String uid;
    Long id;

    public Program(){
    }

    public Program(String name){
        this.name = name;
    }

    public Program(String name, String uid, Long id) {
        this.name = name;
        this.uid = uid;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}