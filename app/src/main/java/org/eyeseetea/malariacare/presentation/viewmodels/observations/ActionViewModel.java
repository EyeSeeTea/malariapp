package org.eyeseetea.malariacare.presentation.viewmodels.observations;

import java.util.Date;

public class ActionViewModel {
    private String description;
    private Date dueDate;
    private String responsible;
    private Date completionDate;

    public ActionViewModel(
            String description, Date dueDate,
            String responsible, Date completionDate) {
        this.description = description;
        this.dueDate = dueDate;
        this.responsible = responsible;
        this.completionDate = completionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public boolean isCompleted() {
        return completionDate != null;
    }

    public void setCompleted(boolean completed) {
        if (completed){
            completionDate = new Date();
        } else {
            completionDate = null;
        }
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public boolean isEmpty(){
        return (description == null || description.isEmpty()) && dueDate == null &&
                (responsible == null || responsible.isEmpty());
    }

    public boolean isValid(){
        return description != null && !description.isEmpty() && dueDate != null &&
                responsible != null && !responsible.isEmpty();
    }
}
