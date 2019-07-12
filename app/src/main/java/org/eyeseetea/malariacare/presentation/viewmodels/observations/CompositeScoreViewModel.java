package org.eyeseetea.malariacare.presentation.viewmodels.observations;

public class CompositeScoreViewModel extends MissedStepViewModel {
    private final String code;
    private boolean expanded = true;

    public CompositeScoreViewModel(long compositeScoreId, long compositeScoreParentId,
            String code, String name) {
        super(COMPOSITE_KEY_PREFIX + compositeScoreId,
                COMPOSITE_KEY_PREFIX + compositeScoreParentId,
                code + " " + name, true);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

}
