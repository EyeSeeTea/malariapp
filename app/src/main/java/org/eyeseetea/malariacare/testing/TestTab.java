package org.eyeseetea.malariacare.testing;

import java.util.List;

/**
 * Created by ignacio on 2/15/15.
 */
public class TestTab {
    private String title;
    private List<TestQuestion> testQuestions;

    public TestTab(){
        this.title = "";
        this.testQuestions = null;
    }

    public TestTab(String title){
        this.title = title;
        this.testQuestions = null;
    }

    public TestTab(String title, List<TestQuestion> testQuestions){
        this.title = title;
        this.testQuestions = testQuestions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TestQuestion> getTestQuestions() {
        return testQuestions;
    }

    public void setTestQuestions(List<TestQuestion> testQuestions) {
        this.testQuestions = testQuestions;
    }
}
