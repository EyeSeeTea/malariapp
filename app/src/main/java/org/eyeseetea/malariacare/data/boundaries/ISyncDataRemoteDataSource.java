package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.ISyncData;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;

import java.util.List;

public interface ISyncDataRemoteDataSource {
    List<? extends ISyncData> get(SurveyFilter filters) throws Exception;
    PushReport save (List<? extends ISyncData> data) throws Exception;
}
