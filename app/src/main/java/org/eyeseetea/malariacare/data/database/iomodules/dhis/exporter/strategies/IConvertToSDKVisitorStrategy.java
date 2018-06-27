package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.strategies;

import android.content.Context;

import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;

import java.util.Date;
import java.util.Map;

public interface IConvertToSDKVisitorStrategy {

    void removeSurveyAndEvent();

    void annotateSurveyAndEvent(Date uploadedDate);

    void saveSurveyStatus(Map<String, PushReport> pushReportMap, final
    IPushController.IPushControllerCallback callback,Context context);

    void setSurveysAsQuarantine();
}
