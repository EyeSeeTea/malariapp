package org.eyeseetea.malariacare.presentation.viewmodels.observations;

public abstract class MissedStepViewModel {

    private final String label;
    private final boolean isCompositeScore;
    private boolean visible = true;
    private String parentKey;
    private String key;

    static String COMPOSITE_KEY_PREFIX = "C-";
    static String QUESTION_KEY_PREFIX = "Q-";

    MissedStepViewModel(
            String key,
            String parentKey,
            String label,
            boolean isCompositeScore) {
        this.key = key;
        this.parentKey = parentKey;
        this.label = label;
        this.isCompositeScore = isCompositeScore;
    }

    public String getKey() {
        return key;
    }

    public String getParentKey() {
        return parentKey;
    }

    public String getLabel() {
        return label;
    }

    public boolean isCompositeScore() {
        return isCompositeScore;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}