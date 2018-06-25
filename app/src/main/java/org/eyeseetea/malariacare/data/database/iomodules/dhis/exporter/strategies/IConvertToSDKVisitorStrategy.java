package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.strategies;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;

import java.util.Map;

public interface IConvertToSDKVisitorStrategy {

    void removeSurveyAndEvent(SurveyDB survey);

    void annotateSurveyAndEvent();

    void saveSurveyStatus(Map<String, PushReport> pushReportMap, final
    IPushController.IPushControllerCallback callback);

    void setSurveysAsQuarantine();
}
