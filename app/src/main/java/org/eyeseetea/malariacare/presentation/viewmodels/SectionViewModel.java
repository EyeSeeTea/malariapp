package org.eyeseetea.malariacare.presentation.viewmodels;

public class SectionViewModel {
    private boolean expanded = true;
    private String title;
    private int color;

    public SectionViewModel(String title, int color) {
        this.title = title;
        this.color = color;
    }

    public boolean isExpanded() {
        return expanded;
    }


    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getTitle() {
        return title;
    }

    public int getColor() {
        return color;
    }
}
