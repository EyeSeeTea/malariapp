package org.eyeseetea.malariacare.data.remote;

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.data.database.datasources.OrgUnitLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.SurveyLocalDataSource;
import org.eyeseetea.malariacare.data.remote.api.SurveyAPIDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.UpdateQuarantineSurveyStatusUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.utils.AUtils;

public class SurveyChecker {
    private static String TAG = ".CheckSurveysB&D";

    /**
     * Launch a new thread to checks all the quarantine surveys
     */
    public static void launchQuarantineChecker(Context context, Credentials credentials) {
        if (!AUtils.isNetworkAvailable()) {
            return;
        }
        try {
            checkAllQuarantineSurveys(context, credentials);
        } finally {
            Log.d(TAG, "Quarantine thread finished");
        }
    }


    /**
     * Download the related events. and checks all the quarantine surveys.
     * If a survey is in the server, the survey should be set as sent. Else, the survey should be
     * set as completed and it will be resend.
     */
    public static void checkAllQuarantineSurveys(Context context, Credentials credentials) {
                IServerMetadataRepository serverMetadataRepository =
                        new ServerMetadataRepository(context);
                ISurveyDataSource localDataSource = new SurveyLocalDataSource();
                ISurveyDataSource apiDataSource = new SurveyAPIDataSource(credentials, serverMetadataRepository);
                IAsyncExecutor asyncExecutor = new AsyncExecutor();
                UpdateQuarantineSurveyStatusUseCase updateQuarantineSurveyStatusUseCase = new UpdateQuarantineSurveyStatusUseCase(
                        asyncExecutor,
                        new OrgUnitLocalDataSource(),
                        localDataSource, apiDataSource);
                updateQuarantineSurveyStatusUseCase.execute();
    }
}
