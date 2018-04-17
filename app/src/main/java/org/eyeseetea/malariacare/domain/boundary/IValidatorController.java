package org.eyeseetea.malariacare.domain.boundary;

public interface IValidatorController {
    interface IValidatorControllerCallback {
        void validate(boolean result);
    }

    void validateTables(IValidatorControllerCallback callback);
    void removeInvalidCS();
}
