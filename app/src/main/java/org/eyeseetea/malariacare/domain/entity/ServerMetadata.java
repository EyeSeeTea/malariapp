package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

import java.util.ArrayList;
import java.util.List;

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
    private final ServerMetadataItem action1;
    private final ServerMetadataItem dueDateAction1;
    private final ServerMetadataItem responsibleAction1;
    private final ServerMetadataItem completionDateAction1;
    private final ServerMetadataItem action2;
    private final ServerMetadataItem dueDateAction2;
    private final ServerMetadataItem responsibleAction2;
    private final ServerMetadataItem completionDateAction2;
    private final ServerMetadataItem action3;
    private final ServerMetadataItem dueDateAction3;
    private final ServerMetadataItem responsibleAction3;
    private final ServerMetadataItem completionDateAction3;

    private final List<String> observationDataElementUids = new ArrayList<>();

    public ServerMetadata(ServerMetadataItem nextAssessment, ServerMetadataItem creationDate,
            ServerMetadataItem completionDate, ServerMetadataItem uploadDate,
            ServerMetadataItem uploadBy, ServerMetadataItem overallScore,
            ServerMetadataItem mainScoreClass, ServerMetadataItem mainScoreA,
            ServerMetadataItem mainScoreB, ServerMetadataItem mainScoreC,
            ServerMetadataItem forwardOrder, ServerMetadataItem pushDevice,
            ServerMetadataItem overallProductivity, ServerMetadataItem provider,
            ServerMetadataItem action1, ServerMetadataItem dueDateAction1,
            ServerMetadataItem responsibleAction1, ServerMetadataItem completionDateAction1,
            ServerMetadataItem action2, ServerMetadataItem dueDateAction2,
            ServerMetadataItem responsibleAction2, ServerMetadataItem completionDateAction2,
            ServerMetadataItem action3, ServerMetadataItem dueDateAction3,
            ServerMetadataItem responsibleAction3, ServerMetadataItem completionDateAction3) {
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

        this.action1 = required(action1, "action1 is required");
        this.dueDateAction1 = required(dueDateAction1, "dueDateAction1 is required");
        this.responsibleAction1 = required(responsibleAction1, "responsibleAction1 is required");
        this.completionDateAction1 = required(completionDateAction1, "completionDateAction1 is required");

        this.action2 = required(action2, "action2 is required");
        this.dueDateAction2 = required(dueDateAction2, "dueDateAction2 is required");
        this.responsibleAction2 = required(responsibleAction2, "responsibleAction2 is required");
        this.completionDateAction2 = required(completionDateAction2, "completionDateAction2 is required");

        this.action3 = required(action3, "action3 is required");
        this.dueDateAction3 = required(dueDateAction3, "dueDateAction3 is required");
        this.responsibleAction3 = required(responsibleAction3, "responsibleAction3 is required");
        this.completionDateAction3 = required(completionDateAction3, "completionDateAction3 is required");

        observationDataElementUids.add(this.provider.getUId());

        observationDataElementUids.add(this.action1.getUId());
        observationDataElementUids.add(this.dueDateAction1.getUId());
        observationDataElementUids.add(this.responsibleAction1.getUId());
        observationDataElementUids.add(this.completionDateAction1.getUId());

        observationDataElementUids.add(this.action2.getUId());
        observationDataElementUids.add(this.dueDateAction2.getUId());
        observationDataElementUids.add(this.responsibleAction2.getUId());
        observationDataElementUids.add(this.completionDateAction2.getUId());

        observationDataElementUids.add(this.action3.getUId());
        observationDataElementUids.add(this.dueDateAction3.getUId());
        observationDataElementUids.add(this.responsibleAction3.getUId());
        observationDataElementUids.add(this.completionDateAction3.getUId());
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

    public ServerMetadataItem getAction1() {
        return action1;
    }

    public ServerMetadataItem getDueDateAction1() {
        return dueDateAction1;
    }

    public ServerMetadataItem getResponsibleAction1() {
        return responsibleAction1;
    }

    public ServerMetadataItem getCompletionDateAction1() {
        return completionDateAction1;
    }

    public ServerMetadataItem getAction2() {
        return action2;
    }

    public ServerMetadataItem getDueDateAction2() {
        return dueDateAction2;
    }

    public ServerMetadataItem getResponsibleAction2() {
        return responsibleAction2;
    }

    public ServerMetadataItem getCompletionDateAction2() {
        return completionDateAction2;
    }

    public ServerMetadataItem getAction3() {
        return action3;
    }

    public ServerMetadataItem getDueDateAction3() {
        return dueDateAction3;
    }

    public ServerMetadataItem getResponsibleAction3() {
        return responsibleAction3;
    }

    public ServerMetadataItem getCompletionDateAction3() {
        return completionDateAction3;
    }

    public List<String> getObservationsDataElementUids() {
        return observationDataElementUids;
    }
}
