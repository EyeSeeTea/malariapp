package org.eyeseetea.malariacare.presentation.viewmodels.Observations;

import java.util.Date;

public class ActionViewModel {
    private  String activityAction;
    private  String subActivityAction;
    private  Date dueDateAction;
    private  String responsibleAction;
    private  boolean realized;

    public ActionViewModel(
            String activityAction ,String subActivityAction, Date dueDateAction,
            String responsibleAction, boolean realized) {
        this.activityAction = activityAction;
        this.subActivityAction = subActivityAction;
        this.dueDateAction = dueDateAction;
        this.responsibleAction = responsibleAction;
        this.realized = realized;
    }

    public String getActivityAction() {
        return activityAction;
    }

    public void setActivityAction(String activityAction) {
        this.activityAction = activityAction;
    }

    public String getSubActivityAction() {
        return subActivityAction;
    }

    public void setSubActivityAction(String subActivityAction) {
        this.subActivityAction = subActivityAction;
    }

    public Date getDueDateAction() {
        return dueDateAction;
    }

    public void setDueDateAction(Date dueDateAction) {
        this.dueDateAction = dueDateAction;
    }

    public String getResponsibleAction() {
        return responsibleAction;
    }

    public void setResponsibleAction(String responsibleAction) {
        this.responsibleAction = responsibleAction;
    }

    public boolean isRealized() {
        return realized;
    }

    public void setRealized(boolean realized) {
        this.realized = realized;
    }
}
