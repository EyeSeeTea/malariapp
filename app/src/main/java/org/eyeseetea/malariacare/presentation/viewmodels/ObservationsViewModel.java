package org.eyeseetea.malariacare.presentation.viewmodels;

import org.eyeseetea.malariacare.domain.entity.ObservationStatus;

public class ObservationsViewModel {

    //long id_obs_action_plan;

    String surveyUid;

    String provider;

    String gaps;

    String planAction;

    String action1;

    String action2;

    ObservationStatus status = ObservationStatus.IN_PROGRESS;

    public ObservationsViewModel(String surveyUid) {
        this.surveyUid = surveyUid;
    }

    public ObservationsViewModel(String surveyUid, String provider, String gaps,
            String planAction, String action1, String action2, ObservationStatus status) {
        this.surveyUid = surveyUid;
        this.provider = provider;
        this.gaps = gaps;
        this.planAction = planAction;
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

    public String getPlanAction() {
        return planAction;
    }

    public void setPlanAction(String planAction) {
        this.planAction = planAction;
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
