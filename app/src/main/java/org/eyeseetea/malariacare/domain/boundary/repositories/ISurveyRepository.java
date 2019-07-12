package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Survey;

import java.util.List;

public interface ISurveyRepository {
    List<Survey> getSurveysByUids(List<String> uids) throws Exception;

    void save(List<Survey> surveys) throws Exception;
}
