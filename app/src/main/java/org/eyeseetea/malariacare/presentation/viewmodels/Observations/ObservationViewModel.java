package org.eyeseetea.malariacare.presentation.viewmodels.Observations;

import org.eyeseetea.malariacare.domain.entity.ObservationStatus;

public class ObservationViewModel {

    private ObservationStatus status = ObservationStatus.IN_PROGRESS;

    private String surveyUid;

    private String provider = "";
    private String actionPlan = "";

    private ActionViewModel action1;
    private ActionViewModel action2;
    private ActionViewModel action3;

    public ObservationViewModel(String surveyUid) {
        this.surveyUid = surveyUid;

        action1 = new ActionViewModel("","",null,"",false);
        action2 = new ActionViewModel("","",null,"",false);
        action3 = new ActionViewModel("","",null,"",false);
    }

    public ObservationViewModel(String surveyUid, String provider, String actionPlan,
            ActionViewModel action1, ActionViewModel action2, ActionViewModel action3,
            ObservationStatus status) {
        this.surveyUid = surveyUid;
        this.provider = provider;
        this.actionPlan = actionPlan;
        this.action1 = action1;
        this.action2 = action2;
        this.action3 = action3;
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

    public String getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(String actionPlan) {
        this.actionPlan = actionPlan;
    }

    public ActionViewModel getAction1() {
        return action1;
    }

    public ActionViewModel getAction2() {
        return action2;
    }

    public ActionViewModel getAction3() {
        return action3;
    }


    public ObservationStatus getStatus() {
        return status;
    }

    public void setStatus(ObservationStatus status) {
        this.status = status;
    }
}
