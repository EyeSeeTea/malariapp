package org.eyeseetea.malariacare;

import java.util.List;

/**
 * Created by ignacio on 2/15/15.
 */
public class Tab {
    private String title;
    private List<Question> questions;

    public Tab(){
        this.title = "";
        this.questions = null;
    }

    public Tab(String title){
        this.title = title;
        this.questions = null;
    }

    public Tab(String title, List<Question> questions){
        this.title = title;
        this.questions = questions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
