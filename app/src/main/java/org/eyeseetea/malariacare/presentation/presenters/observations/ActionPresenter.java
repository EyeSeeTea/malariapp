package org.eyeseetea.malariacare.presentation.presenters.observations;

import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel;

public class ActionPresenter {
    private View view;

    private ActionViewModel actionViewModel;

    private String[] activities;
    private String[] subActivities;

    public void attachView(View view, String[] activities, String[] subActivities){
        this.activities = activities;
        this.subActivities = subActivities;
        this.view = view;

        this.actionViewModel = new ActionViewModel("","",null,"",false);

        showActivitiesAndSubActivities();
    }

    public ActionViewModel getAction(ActionViewModel actionViewModel) {
        return actionViewModel;
    }

    public void setAction(ActionViewModel actionViewModel) {
        this.actionViewModel = actionViewModel;
        showAction();
    }

    public void onActivitySelected(String selectedActivity) {

        if (selectedActivity.equals(activities[0])) {
            selectedActivity = "";
        }

        if (!selectedActivity.equals(actionViewModel.getActivityAction())) {
            actionViewModel.setActivityAction(selectedActivity);
            actionViewModel.setSubActivityAction("");

            view.notifyOnActionChanged(actionViewModel);
            showAction();
        }
    }

    public void onSubActivitySelected(String selectedSubAction) {
        if (selectedSubAction.equals(subActivities[0])) {
            selectedSubAction = "";
        }

        if (!selectedSubAction.equals(actionViewModel.getSubActivityAction())) {
            actionViewModel.setSubActivityAction(selectedSubAction);
            view.notifyOnActionChanged(actionViewModel);
        }
    }

    public void subActivityOtherChanged(String subActionOther) {
        if (!subActionOther.equals(actionViewModel.getSubActivityAction())) {
            actionViewModel.setSubActivityAction(subActionOther);
            view.notifyOnActionChanged(actionViewModel);
        }
    }

    private void showActivitiesAndSubActivities() {
        if (view != null) {
            view.loadActivities(activities);
            view.loadSubActivities(subActivities);
        }
    }

    private void showAction() {
        if (actionViewModel.getActivityAction().isEmpty()) {
            view.selectActivity(0);
        } else {
            for (int i = 0; i < activities.length; i++) {
                if (actionViewModel.getActivityAction().equals(activities[i])) {
                    view.selectActivity(i);
                    break;
                }
            }
        }

        if (actionViewModel.getActivityAction().equals(activities[1])) {
            if (actionViewModel.getSubActivityAction().isEmpty()) {
                view.selectSubActivity(0);
            } else {
                for (int i = 0; i < subActivities.length; i++) {
                    if (actionViewModel.getSubActivityAction().equals(
                            subActivities[i])) {
                        view.selectSubActivity(i);
                        break;
                    }
                }
            }
        } else if (actionViewModel.getActivityAction().equals(activities[5])) {
            view.renderOtherSubActivity(actionViewModel.getSubActivityAction());
        }

        showHideSubActivity();
    }

    private void showHideSubActivity() {
        if (view != null) {
            if (actionViewModel.getActivityAction().equals(activities[1])) {
                view.showSubActivitiesView();
                view.hideSubActivityOtherView();
            } else if (actionViewModel.getActivityAction().equals(activities[5])) {
                view.hideSubActivitiesView();
                view.showSubActivityOtherView();
            } else {
                view.hideSubActivitiesView();
                view.hideSubActivityOtherView();
            }
        }
    }


    public interface View{
        void selectActivity(int position);
        void selectSubActivity(int position);

        void loadActivities(String[] activities);
        void loadSubActivities(String[] subActivities);

        void notifyOnActionChanged(ActionViewModel actionViewModel);

        void showSubActivitiesView();
        void hideSubActivitiesView();

        void showSubActivityOtherView();
        void hideSubActivityOtherView();

        void renderOtherSubActivity(String subActivityAction);
    }
}
