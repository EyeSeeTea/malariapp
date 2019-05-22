package org.eyeseetea.malariacare.presentation.presenters.observations;

import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel;

import java.util.Date;

public class ActionPresenter {
    private View view;

    private ActionViewModel actionViewModel;
    private boolean expanded = false;

    public void attachView(View view){
        this.view = view;

        this.actionViewModel = new ActionViewModel("",null,"", null);

        expandOrCollapse();
    }

    public ActionViewModel getAction(ActionViewModel actionViewModel) {
        return actionViewModel;
    }

    public void setAction(ActionViewModel actionViewModel) {
        this.actionViewModel = actionViewModel;
        showAction();
    }

    public void onDescriptionChange(String description) {
        if (!actionViewModel.getDescription().equals(description)) {
            actionViewModel.setDescription(description);
            view.notifyOnActionChanged(actionViewModel);
        }
    }

    public void onDueDateChange(Date dueDate) {
        if ((actionViewModel.getDueDate() != null &&
                actionViewModel.getDueDate().getTime() != dueDate.getTime()) ||
                (actionViewModel.getDueDate() == null && dueDate != null)) {
            actionViewModel.setDueDate(dueDate);
            view.notifyOnActionChanged(actionViewModel);

        }
    }

    public void onResponsibleChange(String responsible) {
        if (!actionViewModel.getResponsible().equals(responsible)) {
            actionViewModel.setResponsible(responsible);
            view.notifyOnActionChanged(actionViewModel);
        }
    }

    private void showAction() {
        if (view != null){
            view.showActionData(actionViewModel);
        }
    }


    public void expandOrCollapse() {
        if (view != null) {
            if (expanded) {
                view.collapse();
                expanded = false;
            } else {
                view.expand();
                expanded = true;
            }
        }
    }

    public interface View{
        void showActionData(ActionViewModel actionViewModel);
        void notifyOnActionChanged(ActionViewModel actionViewModel);
        void expand();
        void collapse();
    }
}
