package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.strategies;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;

import java.util.List;
import java.util.Map;

public class ConvertToSDKVisitorPlansStrategy implements IConvertToSDKVisitorStrategy {
    private List<SurveyDB> obsActionPlanEvents;
    private SurveyDB currentEvent;

    public ConvertToSDKVisitorPlansStrategy(SurveyDB surveyDB,
            List<SurveyDB> surveyDBList) {

    }

    @Override
    public void removeSurveyAndEvent(SurveyDB survey) {
        if (obsActionPlanEvents.containsKey(currentSurvey.getId_survey())) {
            obsActionPlanEvents.remove(currentSurvey.getId_survey());
        }
        currentEvent.getEvent().delete();
        //remove survey from list and from db
        if (obsActionPlanSurveys.contains(survey)) {
            obsActionPlanSurveys.remove(survey);
        }
    }

    @Override
    public void annotateSurveyAndEvent() {

    }

    @Override
    public void saveSurveyStatus(Map<String, PushReport> pushReportMap,
            IPushController.IPushControllerCallback callback) {

    }

    @Override
    public void setSurveysAsQuarantine() {

    }
}
