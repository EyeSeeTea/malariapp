package org.eyeseetea.malariacare.data.remote;

import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.model.ServerMetadata;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.api.PullDhisApiDataSource;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
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
        if (!AUtils.isNetworkAvailable()) {
            return;
        }
        try {
            int quarantineSurveysSize = Survey.countQuarantineSurveys();
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
        List<Program> programs = Program.getAllPrograms();
        for (Program program : programs) {
            for (OrgUnit orgUnit : program.getOrgUnits()) {
                List<Survey> quarantineSurveys = Survey.getAllQuarantineSurveysByProgramAndOrgUnit(
                        program, orgUnit);
                if (quarantineSurveys.size() == 0) {
                    continue;
                }
                Date minDate = Survey.getMinQuarantineCompletionDateByProgramAndOrgUnit(program,
                        orgUnit);//The start date is the first ascending completion date of all
                // the quarantine surveys
                Date maxDate = Survey.getMaxQuarantineUpdatedDateByProgramAndOrgUnit(program,
                        orgUnit);//The last date is the first descending updated date of all the
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
                        updateQuarantineSurveysStatus(events, survey);
                    } else {
                        changeSurveyStatusFromQuarantineTo(survey, Constants.SURVEY_COMPLETED);
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
    private static void updateQuarantineSurveysStatus(List<EventExtended> events, Survey survey) {
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

    private static void changeSurveyStatusFromQuarantineTo(Survey survey, int status) {
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
    private static boolean surveyDateExistsInEventTimeCaptureControlDE(Survey survey,
            EventExtended event) {
        for (DataValueExtended dataValue : DataValueExtended.getExtendedList(
                event.getDataValuesInMemory())) {
            String uid = ServerMetadata.findControlDataElementUid(
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
