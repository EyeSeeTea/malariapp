package org.eyeseetea.malariacare.presentation.viewmodels.Observations;

public abstract class CriticalMissedStepViewModel {

    private final String label;
    private final boolean isCompositeScore;

    public CriticalMissedStepViewModel(String label, boolean isCompositeScore) {
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
