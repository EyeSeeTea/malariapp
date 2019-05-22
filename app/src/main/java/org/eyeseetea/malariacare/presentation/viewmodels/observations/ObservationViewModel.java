package org.eyeseetea.malariacare.presentation.viewmodels.observations;

import org.eyeseetea.malariacare.domain.entity.ObservationStatus;

public class ObservationViewModel {

    private ObservationStatus status = ObservationStatus.IN_PROGRESS;

    private String surveyUid;

    private String provider = "";

    private ActionViewModel action1;
    private ActionViewModel action2;
    private ActionViewModel action3;

    public ObservationViewModel(String surveyUid) {
        this.surveyUid = surveyUid;

        action1 = new ActionViewModel("",null,"", null);
        action2 = new ActionViewModel("",null,"", null);
        action3 = new ActionViewModel("",null,"", null);
    }

    public ObservationViewModel(String surveyUid, String provider,
            ActionViewModel action1, ActionViewModel action2, ActionViewModel action3,
            ObservationStatus status) {
        this.surveyUid = surveyUid;
        this.provider = provider;
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

    public ActionViewModel getAction1() {
        return action1;
    }

    public void setAction1(ActionViewModel action1) {
        this.action1 = action1;
    }

    public ActionViewModel getAction2() {
        return action2;
    }

    public void setAction2(ActionViewModel action2) {
        this.action2 = action2;
    }

    public ActionViewModel getAction3() {
        return action3;
    }

    public void setAction3(ActionViewModel action3) {
        this.action3 = action3;
    }

    public ObservationStatus getStatus() {
        return status;
    }

    public void setStatus(ObservationStatus status) {
        this.status = status;
    }

    public boolean isValid(){
        boolean atLeastOneFilled = !(action1.isEmpty() && action2.isEmpty() && action3.isEmpty());

        return atLeastOneFilled &&
                (action1.isValid() || action1.isEmpty()) &&
                (action2.isValid() || action2.isEmpty()) &&
                (action3.isValid() || action3.isEmpty());
    }
}
