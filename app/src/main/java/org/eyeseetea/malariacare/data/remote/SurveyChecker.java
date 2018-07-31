package org.eyeseetea.malariacare.data.remote;

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.data.database.datasources.SurveyLocalDataSource;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.SurveyQuarantineRepository;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.api.SurveyAPIDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.ISurveyQuarantineRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.FixQuarantineSurveyStatusUseCase;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

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
                String createdOnuid = serverMetadataRepository.getServerMetadata().getCreationDate().getUId();
                ISurveyDataSource localDataSource = new SurveyLocalDataSource();
                ISurveyDataSource apiDataSource = new SurveyAPIDataSource(credentials, serverMetadataRepository);
                ISurveyQuarantineRepository quarantineExistOnServerController = new SurveyQuarantineRepository(localDataSource, apiDataSource);
                FixQuarantineSurveyStatusUseCase fixQuarantineSurveyStatusUseCase = new FixQuarantineSurveyStatusUseCase(quarantineExistOnServerController, createdOnuid);
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

    /**
     * Given a list of events, check for the presence of that survey among the events, and update
     * consequently their status. If the survey exist (checked by completion date) then it's
     * considered as sent, otherwise it will be considered as just completed and awaiting to be
     * sent
     */
    private static void updateQuarantineSurveysStatus(List<EventExtended> events, SurveyDB survey) {
        boolean isSent = false;
        for (EventExtended event : events) {
            isSent = surveyDateExistsInEventTimeCaptureControlDE(survey, event);
            if (isSent) {
                break;
            }
        }
        //When the completion date for a survey is not present in the server, this survey is
        // not in the server.
        //This survey is set as "completed" and will be send in the future.
        changeSurveyStatusFromQuarantineTo(survey,
                (isSent) ? Constants.SURVEY_SENT : Constants.SURVEY_COMPLETED);
    }

    private static void changeSurveyStatusFromQuarantineTo(SurveyDB survey, int status) {
        try {
            Log.d(TAG, "Set quarantine survey as " + ((status == Constants.SURVEY_SENT) ? "sent "
                    : "complete ") + survey.getId_survey() + " date "
                    + EventExtended.format(survey.getCreationDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (survey.isInQuarantine()) {
            survey.setStatus(status);
            survey.save();
        }
    }

    /**
     * Given an event, check through all its DVs if the survey completion date is present in the
     * event in the form of the control DE "Time Capture" whose UID is hardcoded
     */
    private static boolean surveyDateExistsInEventTimeCaptureControlDE(SurveyDB survey,
            EventExtended event) {
        for (DataValueExtended dataValue : DataValueExtended.getExtendedList(
                event.getDataValuesInMemory())) {
            String uid = ServerMetadataDB.findControlDataElementUid(
                    PreferencesState.getInstance().getContext().getString(
                            R.string.created_on_code));
            if (dataValue.getDataElement().equals(uid)
                    && dataValue.getValue().equals(EventExtended.format(survey.getCreationDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT))) {
                Log.d(TAG, "Found survey" + survey.getId_survey() + "date "
                        + survey.getCreationDate() + "dateevent" + dataValue.getValue());
                return true;
            }
        }
        return false;
    }
}
