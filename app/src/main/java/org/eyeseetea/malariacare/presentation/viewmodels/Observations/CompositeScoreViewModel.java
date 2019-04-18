package org.eyeseetea.malariacare.presentation.viewmodels.Observations;

public class CompositeScoreViewModel extends CriticalMissedStepViewModel {
    private final String code;

    public CompositeScoreViewModel(String code, String name) {
        super(code + " " + name, true);

        this.code = code;
    }


}
