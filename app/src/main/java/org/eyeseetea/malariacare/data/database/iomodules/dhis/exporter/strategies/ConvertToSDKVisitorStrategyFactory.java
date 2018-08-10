package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.strategies;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.IPushController;

import java.util.List;
import java.util.Map;

public class ConvertToSDKVisitorStrategyFactory {
    private Map<Long, EventExtended> obsActionPlanEvents;
    private List<SurveyDB> mSurveyDBList;
    private SurveyDB currentSurvey;
    private EventExtended currentEvent;

    public ConvertToSDKVisitorStrategyFactory(
            Map<Long, EventExtended> obsActionPlanEvents,
            List<SurveyDB> surveyDBList,
            SurveyDB currentSurvey,
            EventExtended currentEvent) {
        this.obsActionPlanEvents = obsActionPlanEvents;
        mSurveyDBList = surveyDBList;
        this.currentSurvey = currentSurvey;
        this.currentEvent = currentEvent;
    }

    public IConvertToSDKVisitorStrategy get(IPushController.Kind kind) {
        if (kind == IPushController.Kind.EVENTS) {
            return new ConvertToSDKVisitorEventStrategy(obsActionPlanEvents, mSurveyDBList,
                    currentSurvey, currentEvent);
        } else {
            return new ConvertToSDKVisitorPlansStrategy(obsActionPlanEvents, mSurveyDBList,
                    currentSurvey, currentEvent);
        }
    }

}
