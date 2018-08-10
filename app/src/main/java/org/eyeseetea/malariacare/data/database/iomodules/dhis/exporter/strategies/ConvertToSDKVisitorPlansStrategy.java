package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.strategies;

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushConflict;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.exception.push.NullEventDateException;
import org.eyeseetea.malariacare.domain.exception.push.PushValueException;
import org.eyeseetea.malariacare.utils.Constants;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertToSDKVisitorPlansStrategy implements IConvertToSDKVisitorStrategy {
    private Map<Long, EventExtended> obsActionPlanEvents;
    private List<SurveyDB> obsActionPlanSurveys;
    private SurveyDB currentSurvey;
    private EventExtended currentEvent;

    public ConvertToSDKVisitorPlansStrategy(
            Map<Long, EventExtended> obsActionPlanEvents,
            List<SurveyDB> obsActionPlanSurveys,
            SurveyDB currentSurvey,
            EventExtended currentEvent) {
        this.obsActionPlanEvents = obsActionPlanEvents;
        this.obsActionPlanSurveys = obsActionPlanSurveys;
        this.currentSurvey = currentSurvey;
        this.currentEvent = currentEvent;
    }

    @Override
    public void removeSurveyAndEvent() {
        if (obsActionPlanEvents.containsKey(currentSurvey.getId_survey())) {
            obsActionPlanEvents.remove(currentSurvey.getId_survey());
        }
        currentEvent.getEvent().delete();
        //remove survey from list and from db
        if (obsActionPlanSurveys.contains(currentSurvey)) {
            obsActionPlanSurveys.remove(currentSurvey);
        }
    }

    @Override
    public void annotateSurveyAndEvent(Date uploadedDate) {
        obsActionPlanSurveys.add(currentSurvey);
        uploadedDate = currentSurvey.getUploadDate();
        currentEvent.setLastUpdated(new DateTime(uploadedDate.getTime()));
        obsActionPlanEvents.put(currentSurvey.getId_survey(), currentEvent);
        Log.d(getClass().getName(),
                String.format("%d plan surveys converted so far", obsActionPlanSurveys.size()));
    }

    @Override
    public void saveSurveyStatus(Map<String, PushReport> pushReportMap,
            IPushController.IPushControllerCallback callback, Context context,
            Date uploadedDate) {
        List<SurveyDB> surveys = new ArrayList<>();
        Map<Long, EventExtended> events = new HashMap<>();
            surveys=this.obsActionPlanSurveys;
            events=this.obsActionPlanEvents;
        Log.d(getClass().getName(), String.format("pushReportMap %d surveys savedSurveyStatus", surveys.size()));
        for (int i = 0; i < surveys.size(); i++) {
            SurveyDB iSurvey = surveys.get(i);
            ObsActionPlanDB obsActionPlan = ObsActionPlanDB.findObsActionPlanBySurvey(iSurvey.getId_survey());

            //Sets the survey status as quarantine to prevent wrong reports on unexpected exception.
            //F.E. if the app crash unexpected this survey will be checked again in the future push to prevent the duplicates
            // in the server.


            Log.d(getClass().getName(), "saveSurveyStatus: Starting saving survey Set Survey status as QUARANTINE"
                    + iSurvey.getId_survey() + " eventuid: " + iSurvey.getEventUid());
            EventExtended iEvent = new EventExtended(events.get(iSurvey.getId_survey()));
            PushReport pushReport;
            pushReport = pushReportMap.get(
                    iEvent.getEvent().getUId());
            if (pushReport == null) {
                //the survey was saved as quarantine.
                Log.d(getClass().getName(),"Error saving survey: report is null in this survey: " + iSurvey.getId_survey());
                //The loop should continue without throw the Exception.
                continue;
            }
            List<PushConflict> pushConflicts = pushReport.getPushConflicts();

            //If the pushResult has some conflict the survey was saved in the server but
            // never resend, the survey is saved as survey in conflict.
            if (pushConflicts != null && pushConflicts.size() > 0) {
                Log.d(getClass().getName(), "saveSurveyStatus: conflicts");
                    obsActionPlan.setStatus(Constants.SURVEY_CONFLICT);
                    obsActionPlan.save();

                for (PushConflict pushConflict : pushConflicts) {
                    Log.d(getClass().getName(), "saveSurveyStatus: Faileditem not null " + iSurvey.getId_survey());
                    if (pushConflict.getUid() != null) {
                        Log.d(getClass().getName(), "saveSurveyStatus: PUSH process...PushConflict in "
                                + pushConflict.getUid() +
                                " with error " + pushConflict.getValue()
                                + " dataelement pushing survey: "
                                + iSurvey.getId_survey());
                        iSurvey.saveConflict(pushConflict.getUid());
                        iSurvey.save();
                        callback.onInformativeError(new PushValueException(
                                String.format(context.getString(R.string.error_conflict_message),
                                        iEvent.getEvent().getUId(), pushConflict.getUid(),
                                        pushConflict.getValue()) + ""));
                    }
                }
                continue;
            }

            //No errors -> Save and next
            Boolean emptyImportAllowed = false;
                emptyImportAllowed = true;

            if (pushReport!=null && !pushReport.hasPushErrors(emptyImportAllowed)) {
                Log.d(getClass().getName(), "saveSurveyStatus: report without errors and status ok "
                        + iSurvey.getId_survey());
                if (iEvent.getEventDate() == null || iEvent.getEventDate().equals("")) {
                    //If eventdate is null the event is invalid. The event is sent but we need inform to the user.
                    callback.onInformativeError(new NullEventDateException(
                            String.format(context.getString(R.string.error_message_push),
                                    iEvent.getEvent())));
                }
                    obsActionPlan.setStatus(Constants.SURVEY_SENT);
                    obsActionPlan.save();

            }
        }
    }

    @Override
    public void setSurveysAsQuarantine() {
        List<SurveyDB> surveys = new ArrayList<>();
        surveys = this.obsActionPlanSurveys;
        for (SurveyDB survey : surveys) {
            Log.d(getClass().getName(), "Set plan status as QUARANTINE" + survey.getId_survey());
            Log.d(getClass().getName(), "Set plan status as QUARANTINE" + survey.toString());
            ObsActionPlanDB obsActionPlan = ObsActionPlanDB.findObsActionPlanBySurvey(
                    survey.getId_survey());
            //The obs action plan doesn't need the quarantine status.
            obsActionPlan.setStatus(Constants.SURVEY_COMPLETED);
            obsActionPlan.save();

        }
    }
}
