package org.eyeseetea.malariacare.domain.entity;

public interface ISyncData {
    String getSurveyUid();
    void markAsSending();
    void markAsErrorConversionSync();
    void markAsRetrySync();
    void markAsSent();
    void markAsConflict();
    void markValueAsConflict(String uid);
}