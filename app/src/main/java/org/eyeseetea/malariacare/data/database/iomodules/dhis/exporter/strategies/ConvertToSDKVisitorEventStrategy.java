package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.strategies;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;

import java.util.List;
import java.util.Map;

public class ConvertToSDKVisitorEventStrategy implements IConvertToSDKVisitorStrategy {
    public ConvertToSDKVisitorEventStrategy(SurveyDB surveyDB,
            List<SurveyDB> surveyDBList) {

    }

    @Override
    public void removeSurveyAndEvent(SurveyDB survey) {

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
