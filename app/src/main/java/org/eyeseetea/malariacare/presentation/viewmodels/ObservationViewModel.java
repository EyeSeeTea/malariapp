package org.eyeseetea.malariacare.presentation.viewmodels;

import org.eyeseetea.malariacare.domain.entity.ObservationStatus;

public class ObservationViewModel {

    String surveyUid;

    String provider;

    String gaps;

    String actionPlan;

    String action1;

    String action2;

    ObservationStatus status = ObservationStatus.IN_PROGRESS;

    public ObservationViewModel(String surveyUid) {
        this.surveyUid = surveyUid;
    }

    public ObservationViewModel(String surveyUid, String provider, String gaps,
            String actionPlan, String action1, String action2, ObservationStatus status) {
        this.surveyUid = surveyUid;
        this.provider = provider;
        this.gaps = gaps;
        this.actionPlan = actionPlan;
        this.action1 = action1;
        this.action2 = action2;
        this.status = status;
    }

    public String getSurveyUid() {
        return surveyUid;
    }

    public void setSurveyUid(String surveyUid) {
        this.surveyUid = surveyUid;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getGaps() {
        return gaps;
    }

    public void setGaps(String gaps) {
        this.gaps = gaps;
    }

    public String getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(String actionPlan) {
        this.actionPlan = actionPlan;
    }

    public String getAction1() {
        return action1;
    }

    public void setAction1(String action1) {
        this.action1 = action1;
    }

    public String getAction2() {
        return action2;
    }

    public void setAction2(String action2) {
        this.action2 = action2;
    }

    public ObservationStatus getStatus() {
        return status;
    }

    public void setStatus(ObservationStatus status) {
        this.status = status;
    }
}
