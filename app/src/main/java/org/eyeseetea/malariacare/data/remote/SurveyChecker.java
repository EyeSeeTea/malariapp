package org.eyeseetea.malariacare.data.remote;

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.data.database.datasources.OrgUnitLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.SurveyLocalDataSource;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.SurveyQuarantineRepository;
import org.eyeseetea.malariacare.data.remote.api.SurveyAPIDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.ISurveyQuarantineRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.FixQuarantineSurveyStatusUseCase;
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
                ISurveyQuarantineRepository quarantineExistOnServerController = new SurveyQuarantineRepository(localDataSource, apiDataSource);
                FixQuarantineSurveyStatusUseCase fixQuarantineSurveyStatusUseCase = new FixQuarantineSurveyStatusUseCase(
                        quarantineExistOnServerController,
                        new OrgUnitLocalDataSource());
                fixQuarantineSurveyStatusUseCase.execute(new FixQuarantineSurveyStatusUseCase.Callback() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "survey checker compeleted");
                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, "survey checker error");
                    }
                });
    }
}
