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
    private final ServerMetadataItem activityAction1;
    private final ServerMetadataItem subActivityAction1;
    private final ServerMetadataItem dueDateAction1;
    private final ServerMetadataItem responsibleAction1;
    private final ServerMetadataItem realizedAction1;
    private final ServerMetadataItem activityAction2;
    private final ServerMetadataItem subActivityAction2;
    private final ServerMetadataItem dueDateAction2;
    private final ServerMetadataItem responsibleAction2;
    private final ServerMetadataItem realizedAction2;
    private final ServerMetadataItem activityAction3;
    private final ServerMetadataItem subActivityAction3;
    private final ServerMetadataItem dueDateAction3;
    private final ServerMetadataItem responsibleAction3;
    private final ServerMetadataItem realizedAction3;

    public ServerMetadata(ServerMetadataItem nextAssessment, ServerMetadataItem creationDate,
            ServerMetadataItem completionDate, ServerMetadataItem uploadDate,
            ServerMetadataItem uploadBy, ServerMetadataItem overallScore,
            ServerMetadataItem mainScoreClass, ServerMetadataItem mainScoreA,
            ServerMetadataItem mainScoreB, ServerMetadataItem mainScoreC,
            ServerMetadataItem forwardOrder, ServerMetadataItem pushDevice,
            ServerMetadataItem overallProductivity, ServerMetadataItem provider,
            ServerMetadataItem gaps, ServerMetadataItem planAction,
            ServerMetadataItem activityAction1, ServerMetadataItem subActivityAction1,
            ServerMetadataItem dueDateAction1, ServerMetadataItem responsibleAction1,
            ServerMetadataItem realizedAction1, ServerMetadataItem activityAction2,
            ServerMetadataItem subActivityAction2, ServerMetadataItem dueDateAction2,
            ServerMetadataItem responsibleAction2, ServerMetadataItem realizedAction2,
            ServerMetadataItem activityAction3, ServerMetadataItem subActivityAction3,
            ServerMetadataItem dueDateAction3, ServerMetadataItem responsibleAction3,
            ServerMetadataItem realizedAction3) {
        this.nextAssessment = required(nextAssessment, "nextAssessment is required");
        this.creationDate = required(creationDate, "creationDate is required");
        this.completionDate = required(completionDate, "completionDate is required");
        this.provider = required(provider, "provider is required");
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

        this.activityAction1 = required(activityAction1, "activityAction1 is required");
        this.subActivityAction1 = required(subActivityAction1, "subActivityAction1 is required");
        this.dueDateAction1 = required(dueDateAction1, "dueDateAction1 is required");
        this.responsibleAction1 = required(responsibleAction1, "subActivityAction1 is required");
        this.realizedAction1 = required(realizedAction1, "realizedAction1 is required");

        this.activityAction2 = required(activityAction2, "activityAction2 is required");
        this.subActivityAction2 = required(subActivityAction2, "subActivityAction2 is required");
        this.dueDateAction2 = required(dueDateAction2, "dueDateAction2 is required");
        this.responsibleAction2 = required(responsibleAction2, "responsibleAction2 is required");
        this.realizedAction2 = required(realizedAction2, "realizedAction2 is required");

        this.activityAction3 = required(activityAction3, "activityAction3 is required");
        this.subActivityAction3 = required(subActivityAction3, "subActivityAction3 is required");
        this.dueDateAction3 = required(dueDateAction3, "dueDateAction3 is required");
        this.responsibleAction3 = required(responsibleAction3, "responsibleAction3 is required");
        this.realizedAction3 = required(realizedAction3, "realizedAction3 is required");
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

    public ServerMetadataItem getActivityAction1() {
        return activityAction1;
    }

    public ServerMetadataItem getSubActivityAction1() {
        return subActivityAction1;
    }

    public ServerMetadataItem getDueDateAction1() {
        return dueDateAction1;
    }

    public ServerMetadataItem getResponsibleAction1() {
        return responsibleAction1;
    }

    public ServerMetadataItem getRealizedAction1() {
        return realizedAction1;
    }

    public ServerMetadataItem getActivityAction2() {
        return activityAction2;
    }

    public ServerMetadataItem getSubActivityAction2() {
        return subActivityAction2;
    }

    public ServerMetadataItem getDueDateAction2() {
        return dueDateAction2;
    }

    public ServerMetadataItem getResponsibleAction2() {
        return responsibleAction2;
    }

    public ServerMetadataItem getRealizedAction2() {
        return realizedAction2;
    }

    public ServerMetadataItem getActivityAction3() {
        return activityAction3;
    }

    public ServerMetadataItem getSubActivityAction3() {
        return subActivityAction3;
    }

    public ServerMetadataItem getDueDateAction3() {
        return dueDateAction3;
    }

    public ServerMetadataItem getResponsibleAction3() {
        return responsibleAction3;
    }

    public ServerMetadataItem getRealizedAction3() {
        return realizedAction3;
    }
}
