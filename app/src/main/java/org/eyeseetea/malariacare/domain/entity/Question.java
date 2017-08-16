package org.eyeseetea.malariacare.domain.entity;

public class Question {
    private boolean compulsory;
    private long id;
    private String uId;

    public Question(long id, String uid){
        this.id = id;
        this.uId = uid;
    }

    public boolean isCompulsory() {
        return compulsory;
    }

    public void setCompulsory(boolean compulsory) {
        this.compulsory = compulsory;
    }

    public long getId() {
        return id;
    }

    public String getUId() {
        return uId;
    }
}