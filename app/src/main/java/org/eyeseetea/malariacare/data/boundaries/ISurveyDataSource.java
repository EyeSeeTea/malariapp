package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;

import java.util.List;

public interface ISurveyDataSource{
    List<Survey> getSurveys(PullFilters filters) throws Exception;
    void Save(List<Survey> surveys) throws Exception;
}
