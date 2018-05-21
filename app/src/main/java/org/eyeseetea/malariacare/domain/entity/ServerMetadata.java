package org.eyeseetea.malariacare.domain.entity;

public class ServerMetadata {
    private final String nextAssessmentUid;
    private final String creationDateUid;
    private final String completionDateUid;
    private final String uploadDateUid;
    private final String uploadByUid;

    public ServerMetadata(String nextAssessmentUid, String creationDateUid,
            String completionDateUid, String uploadDateUid, String uploadByUid) {
        this.nextAssessmentUid = nextAssessmentUid;
        this.creationDateUid = creationDateUid;
        this.completionDateUid = completionDateUid;
        this.uploadDateUid = uploadDateUid;
        this.uploadByUid = uploadByUid;
    }

    public String getNextAssessmentUid() {
        return nextAssessmentUid;
    }

    public String getCreationDateUid() {
        return creationDateUid;
    }

    public String getCompletionDateUid() {
        return completionDateUid;
    }

    public String getUploadDateUid() {
        return uploadDateUid;
    }

    public String getUploadByUid() {
        return uploadByUid;
    }
}
