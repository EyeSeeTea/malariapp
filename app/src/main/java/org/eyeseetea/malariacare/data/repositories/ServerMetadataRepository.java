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
        ServerMetadataDB activityAction1 = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.action1_code));
        ServerMetadataDB subActivityAction1 = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.action2_code));

        ServerMetadataDB action1_1_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.action1_1_code));
        ServerMetadataDB due_date_1_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.due_date_1_code));
        ServerMetadataDB responsible_1_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.responsible_1_code));
        ServerMetadataDB completion_date_1_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.completion_date_1_code));

        ServerMetadataDB action2_1_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.action2_1_code));
        ServerMetadataDB due_date_2_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.due_date_2_code));
        ServerMetadataDB responsible_2_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.responsible_2_code));
        ServerMetadataDB completion_date_2_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.completion_date_2_code));

        ServerMetadataDB action3_1_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.action3_1_code));
        ServerMetadataDB due_date_3_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.due_date_3_code));
        ServerMetadataDB responsible_3_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.responsible_3_code));
        ServerMetadataDB completion_date_3_code = ServerMetadataDB.findControlDataElementByCode(
                context.getString(R.string.completion_date_3_code));

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
                new ServerMetadataItem(action1_1_code.getCode(), action1_1_code.getUid()),
                new ServerMetadataItem(due_date_1_code.getCode(), due_date_1_code.getUid()),
                new ServerMetadataItem(responsible_1_code.getCode(), responsible_1_code.getUid()),
                new ServerMetadataItem(completion_date_1_code.getCode(), completion_date_1_code.getUid()),
                new ServerMetadataItem(action2_1_code.getCode(), action2_1_code.getUid()),
                new ServerMetadataItem(due_date_2_code.getCode(), due_date_2_code.getUid()),
                new ServerMetadataItem(responsible_2_code.getCode(), responsible_2_code.getUid()),
                new ServerMetadataItem(completion_date_2_code.getCode(), completion_date_2_code.getUid()),
                new ServerMetadataItem(action3_1_code.getCode(), action3_1_code.getUid()),
                new ServerMetadataItem(due_date_3_code.getCode(), due_date_3_code.getUid()),
                new ServerMetadataItem(responsible_3_code.getCode(), responsible_3_code.getUid()),
                new ServerMetadataItem(completion_date_3_code.getCode(), completion_date_3_code.getUid())
        );
        return serverMetadata;
    }
}
