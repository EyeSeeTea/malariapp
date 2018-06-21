package org.eyeseetea.malariacare.data.remote;

import static org.eyeseetea.malariacare.domain.entity.Survey.Status.COMPLETED;
import static org.eyeseetea.malariacare.domain.entity.Survey.Status.SENT;

import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.datasources.SurveyDataSource;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.api.PullDhisApiDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.utils.AUtils;
import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class SurveyChecker {
    private static String TAG = ".CheckSurveysB&D";


    /**
     * Launch a new thread to checks all the quarantine surveys
     */
    public static void launchQuarantineChecker() {
        ISurveyRepository surveyRepository = new SurveyDataSource();
        if (!AUtils.isNetworkAvailable()) {
            return;
        }
        try {
            int quarantineSurveysSize = surveyRepository.getQuarantineSurveys().size();
            Log.d(TAG, "Quarantine size: " + quarantineSurveysSize);
            if (quarantineSurveysSize > 0) {
                checkAllQuarantineSurveys();
            }
        } finally {
            Log.d(TAG, "Quarantine thread finished");
        }
    }


    /**
     * Download the related events. and checks all the quarantine surveys.
     * If a survey is in the server, the survey should be set as sent. Else, the survey should be
     * set as completed and it will be resend.
     */
    public static void checkAllQuarantineSurveys() {
        ISurveyRepository surveyRepository = new SurveyDataSource();
        List<ProgramDB> programs = ProgramDB.getAllPrograms();
        for (ProgramDB program : programs) {
            for (OrgUnitDB orgUnit : program.getOrgUnits()) {
                List<Survey> quarantineSurveys =
                        surveyRepository.getQuarantineSurveysByProgramAndOrgUnit(program.getUid(),
                                orgUnit.getUid());
                if (quarantineSurveys.size() == 0) {
                    continue;
                }
                Date minDate = surveyRepository.getMinQuarantineCompletionDateByProgramAndOrgUnit(
                        program.getUid(),
                        orgUnit.getUid()).getCompletionDate();//The start date is the first ascending completion date of all
                // the quarantine surveys
                Date maxDate = surveyRepository.getMaxQuarantineUpdatedDateByProgramAndOrgUnit(
                        program.getUid(),
                        orgUnit.getUid()).getUpdateDate();//The last date is the first descending updated date of all the
                // quarantine surveys
                List<EventExtended> events = null;
                try {
                    events = PullDhisApiDataSource.getEvents(program.getUid(), orgUnit.getUid(),
                            minDate,
                            maxDate);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (events == null) {
                    return;
                }
                for (Survey survey : quarantineSurveys) {
                    if (events.size() > 0) {
                        updateQuarantineSurveysStatus(events, survey, surveyRepository);
                    } else {
                        changeSurveyStatusFromQuarantineTo(survey, COMPLETED, surveyRepository);
                    }
                }
            }
        }
    }

    /**
     * Given a list of events, check for the presence of that survey among the events, and update
     * consequently their status. If the survey exist (checked by completion date) then it's
     * considered as sent, otherwise it will be considered as just completed and awaiting to be
     * sent
     */
    private static void updateQuarantineSurveysStatus(List<EventExtended> events, Survey survey,
            ISurveyRepository surveyRepository) {
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
                (isSent) ? SENT : COMPLETED, surveyRepository);
    }

    private static void changeSurveyStatusFromQuarantineTo(Survey survey, Survey.Status status,
            ISurveyRepository surveyRepository) {
        try {
            Log.d(TAG, "Set quarantine survey as " + ((status == SENT) ? "sent "
                    : "complete ") + survey.getUId() + " date "
                    + EventExtended.format(survey.getCreationDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (survey.isQuarantine()) {
            survey.changeStatus(status);
            surveyRepository.saveOldSurvey(survey);
        }
    }

    /**
     * Given an event, check through all its DVs if the survey completion date is present in the
     * event in the form of the control DE "Time Capture" whose UID is hardcoded
     */
    private static boolean surveyDateExistsInEventTimeCaptureControlDE(Survey survey,
            EventExtended event) {
        for (DataValueExtended dataValue : DataValueExtended.getExtendedList(
                event.getDataValuesInMemory())) {
            String uid = ServerMetadataDB.findControlDataElementUid(
                    PreferencesState.getInstance().getContext().getString(
                            R.string.created_on_code));
            if (dataValue.getDataElement().equals(uid)
                    && dataValue.getValue().equals(EventExtended.format(survey.getCreationDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT))) {
                Log.d(TAG, "Found survey" + survey.getUId() + "date "
                        + survey.getCreationDate() + "dateevent" + dataValue.getValue());
                return true;
            }
        }
        return false;
    }
}
