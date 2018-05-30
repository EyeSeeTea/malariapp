package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class ServerMetadata {
    private final ServerMetadataItem nextAssessment;
    private final ServerMetadataItem creationDate;
    private final ServerMetadataItem completionDate;
    private final ServerMetadataItem uploadDate;
    private final ServerMetadataItem uploadBy;
    private final ServerMetadataItem overallScore;
    private final ServerMetadataItem mainScoreClass;
    private final ServerMetadataItem mainScoreA;
    private final ServerMetadataItem mainScoreB;
    private final ServerMetadataItem mainScoreC;
    private final ServerMetadataItem forwardOrder;
    private final ServerMetadataItem pushDevice;
    private final ServerMetadataItem overallProductivity;
    private final ServerMetadataItem provider;
    private final ServerMetadataItem gaps;
    private final ServerMetadataItem planAction;
    private final ServerMetadataItem action1;
    private final ServerMetadataItem action2;

    public ServerMetadata(ServerMetadataItem nextAssessment, ServerMetadataItem creationDate, ServerMetadataItem completionDate, ServerMetadataItem uploadDate,
                          ServerMetadataItem uploadBy, ServerMetadataItem overallScore, ServerMetadataItem mainScoreClass, ServerMetadataItem mainScoreA,
                          ServerMetadataItem mainScoreB, ServerMetadataItem mainScoreC, ServerMetadataItem forwardOrder, ServerMetadataItem pushDevice,
                          ServerMetadataItem overallProductivity, ServerMetadataItem provider, ServerMetadataItem gaps,
                          ServerMetadataItem planAction, ServerMetadataItem action1, ServerMetadataItem action2) {
        this.nextAssessment = required(nextAssessment, "nextAssessment is required");
        this.creationDate = required(creationDate, "creationDate is required");

        //TODO: temporal comment because this required current throw exception because
        // on database does not exist row, resolving in issue
        // https://github.com/EyeSeeTea/malariapp/issues/1995
        //this.completionDate = required(completionDate, "completionDate is required");
        //this.provider = required(provider, "provider is required");

        this.completionDate = completionDate;
        this.provider = provider;

        this.uploadDate = required(uploadDate, "uploadDate is required");
        this.uploadBy = required(uploadBy, "uploadBy is required");
        this.overallScore = required(overallScore, "overallScore is required");
        this.mainScoreClass = required(mainScoreClass, "mainScoreClass is required");
        this.mainScoreA = required(mainScoreA, "mainScoreA is required");
        this.mainScoreB = required(mainScoreB, "mainScoreB is required");
        this.mainScoreC = required(mainScoreC, "mainScoreC is required");
        this.forwardOrder = required(forwardOrder, "forwardOrder is required");
        this.pushDevice = required(pushDevice, "pushDevice is required");
        this.overallProductivity = required(overallProductivity, "overallProductivity is required");
        this.gaps = required(gaps, "gaps is required");
        this.planAction = required(planAction, "planAction is required");
        this.action1 = required(action1, "action1 is required");
        this.action2 = required(action2, "action2 is required");
    }

    public ServerMetadataItem getNextAssessment() {
        return nextAssessment;
    }

    public ServerMetadataItem getCreationDate() {
        return creationDate;
    }

    public ServerMetadataItem getCompletionDate() {
        return completionDate;
    }

    public ServerMetadataItem getUploadDate() {
        return uploadDate;
    }

    public ServerMetadataItem getUploadBy() {
        return uploadBy;
    }

    public ServerMetadataItem getOverallScore() {
        return overallScore;
    }

    public ServerMetadataItem getMainScoreClass() {
        return mainScoreClass;
    }

    public ServerMetadataItem getMainScoreA() {
        return mainScoreA;
    }

    public ServerMetadataItem getMainScoreB() {
        return mainScoreB;
    }

    public ServerMetadataItem getMainScoreC() {
        return mainScoreC;
    }

    public ServerMetadataItem getForwardOrder() {
        return forwardOrder;
    }

    public ServerMetadataItem getPushDevice() {
        return pushDevice;
    }

    public ServerMetadataItem getOverallProductivity() {
        return overallProductivity;
    }

    public ServerMetadataItem getProvider() {
        return provider;
    }

    public ServerMetadataItem getGaps() {
        return gaps;
    }

    public ServerMetadataItem getPlanAction() {
        return planAction;
    }

    public ServerMetadataItem getAction1() {
        return action1;
    }

    public ServerMetadataItem getAction2() {
        return action2;
    }
}
