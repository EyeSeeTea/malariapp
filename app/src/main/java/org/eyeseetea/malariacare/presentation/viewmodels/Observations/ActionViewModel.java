package org.eyeseetea.malariacare.presentation.viewmodels.observations;

import java.util.Date;

public class ActionViewModel {
    private  String description;
    private  Date dueDate;
    private  String responsible;

    public ActionViewModel(
            String description, Date dueDate,
            String responsible) {
        this.description = description;
        this.dueDate = dueDate;
        this.responsible = responsible;
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
}
