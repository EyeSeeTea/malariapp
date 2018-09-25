package org.eyeseetea.malariacare.domain.entity;

import java.util.Date;

public interface ISyncData {
    String getSurveyUid();
    void markAsSending();
    void markAsErrorConversionSync();
    void markAsRetrySync();
    void markAsSent();
    void markAsConflict();
    void markValueAsConflict(String uid);
    void assignUploadDate(Date date);
}