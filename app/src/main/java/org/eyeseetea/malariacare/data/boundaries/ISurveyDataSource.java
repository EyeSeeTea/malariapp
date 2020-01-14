package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;

import java.util.List;

public interface ISurveyDataSource{
    List<Survey> getSurveysByStatus(SurveyStatus surveyStatus) throws Exception;

    List<Survey> getSurveysByUids(List<String> uids) throws Exception;
    void save(List<Survey> surveys) throws Exception;

    Survey getSurveyByUid(String uid);
}