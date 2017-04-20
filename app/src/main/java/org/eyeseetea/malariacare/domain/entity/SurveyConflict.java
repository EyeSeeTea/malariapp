package org.eyeseetea.malariacare.domain.entity;

public class SurveyConflict {
    private String uid;

    private String value;

    public SurveyConflict(String object, String value) {
        this.uid = object;
        this.value = value;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
