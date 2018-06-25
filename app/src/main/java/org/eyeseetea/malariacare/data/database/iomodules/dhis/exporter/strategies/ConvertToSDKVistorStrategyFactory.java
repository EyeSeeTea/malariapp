package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.strategies;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.IPushController;

import java.util.List;

public class ConvertToSDKVistorStrategyFactory {
    private SurveyDB mSurveyDB;
    private List<SurveyDB> mSurveyDBList;

    public ConvertToSDKVistorStrategyFactory(
            SurveyDB surveyDB, List<SurveyDB> surveyDBList) {
        mSurveyDB = surveyDB;
        mSurveyDBList = surveyDBList;
    }

    public IConvertToSDKVisitorStrategy get(IPushController.Kind kind) {
        if (kind == IPushController.Kind.EVENTS) {
            return new ConvertToSDKVisitorEventStrategy(mSurveyDB, mSurveyDBList);
        } else {
            return new ConvertToSDKVisitorPlansStrategy(mSurveyDB, mSurveyDBList);
        }
    }

}
