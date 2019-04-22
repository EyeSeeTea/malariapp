package org.eyeseetea.malariacare.data.repositories;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.ServerMetadataItem;


public class ServerMetadataRepository implements IServerMetadataRepository {

    Context context;

    public ServerMetadataRepository(Context context) {
        this.context = context;
    }

    @Override
    public ServerMetadata getServerMetadata() {
        ServerMetadataDB overallScore = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.overall_score_code));
        ServerMetadataDB mainScoreClass = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.main_score_class_code));
        ServerMetadataDB mainScoreA = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.main_score_a_code));
        ServerMetadataDB mainScoreB = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.main_score_b_code));
        ServerMetadataDB mainScoreC = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.main_score_c_code));
        ServerMetadataDB forwardOrder = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.forward_order_code));
        ServerMetadataDB pushDevice = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.push_device_code));
        ServerMetadataDB overallProductivity = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.overall_productivity_code));
        ServerMetadataDB nextAssessment = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.next_assessment_code));
        ServerMetadataDB createdOn = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.created_on_code));
        ServerMetadataDB updatedDate = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.upload_date_code));
        ServerMetadataDB completionDate = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.completed_on_code));
        ServerMetadataDB updatedUser = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.uploaded_by_code));
        ServerMetadataDB provider = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.providerCode));
        ServerMetadataDB gaps = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.gaps_code));
        ServerMetadataDB planAction = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.action_plan_code));
        ServerMetadataDB activityAction1 = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.action1_code));
        ServerMetadataDB subActivityAction1 = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.action2_code));

        //TODO: Add new CDE for actions 1,2,3

        ServerMetadata serverMetadata = new ServerMetadata(
                new ServerMetadataItem(nextAssessment.getCode(), nextAssessment.getUid()),
                new ServerMetadataItem(createdOn.getCode(), createdOn.getUid()),
                new ServerMetadataItem(completionDate.getCode(), completionDate.getUid()),
                new ServerMetadataItem(updatedDate.getCode(), updatedDate.getUid()),
                new ServerMetadataItem(updatedUser.getCode(), updatedUser.getUid()),
                new ServerMetadataItem(overallScore.getCode(), overallScore.getUid()),
                new ServerMetadataItem(mainScoreClass.getCode(), mainScoreClass.getUid()),
                new ServerMetadataItem(mainScoreA.getCode(), mainScoreA.getUid()),
                new ServerMetadataItem(mainScoreB.getCode(), mainScoreB.getUid()),
                new ServerMetadataItem(mainScoreC.getCode(), mainScoreC.getUid()),
                new ServerMetadataItem(forwardOrder.getCode(), forwardOrder.getUid()),
                new ServerMetadataItem(pushDevice.getCode(), pushDevice.getUid()),
                new ServerMetadataItem(overallProductivity.getCode(), overallProductivity.getUid()),
                new ServerMetadataItem(provider.getCode(), provider.getUid()),
                new ServerMetadataItem(gaps.getCode(), gaps.getUid()),
                new ServerMetadataItem(planAction.getCode(), planAction.getUid()),
                new ServerMetadataItem(activityAction1.getCode(), activityAction1.getUid()),
                new ServerMetadataItem(subActivityAction1.getCode(), subActivityAction1.getUid()),
                new ServerMetadataItem("_DueDateAction1", "FAKE1"),
                new ServerMetadataItem("_ResponsibleAction1", "FAKE2"),
                new ServerMetadataItem("_isRealizedAction1", "FAKE3"),
                new ServerMetadataItem("_activityAction2", "FAKE4"),
                new ServerMetadataItem("_subactivityAction2", "FAKE5"),
                new ServerMetadataItem("_DueDateAction2", "FAKE6"),
                new ServerMetadataItem("_ResponsibleAction2", "FAKE7"),
                new ServerMetadataItem("_isRealizedAction2", "FAKE8"),
                new ServerMetadataItem("_activityAction3", "FAKE9"),
                new ServerMetadataItem("_subactivityAction3", "FAKE10"),
                new ServerMetadataItem("_DueDateAction3", "FAKE11"),
                new ServerMetadataItem("_ResponsibleAction3", "FAKE12"),
                new ServerMetadataItem("_isRealizedAction3", "FAKE13")
        );
        return  serverMetadata;
    }
}
