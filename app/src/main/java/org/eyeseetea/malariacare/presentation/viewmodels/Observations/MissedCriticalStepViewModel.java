package org.eyeseetea.malariacare.presentation.viewmodels.Observations;

public abstract class MissedCriticalStepViewModel {

    private final String label;
    private final boolean isCompositeScore;

    public MissedCriticalStepViewModel(String label, boolean isCompositeScore) {
        this.label = label;
        this.isCompositeScore = isCompositeScore;
    }

    public String getLabel() {
        return label;
    }

    public boolean isCompositeScore() {
        return isCompositeScore;
    }
}
