package org.eyeseetea.malariacare.data.sync;

public interface IData {
    Long getSurveyId();
    void changeStatusToSending();
    void changeStatusToSyncConversionError();
    void changeStatusToQuarantine();
    void changeStatusToConflict();
    void changeStatusToSent();
    void saveConflict(String questionUid);
}
