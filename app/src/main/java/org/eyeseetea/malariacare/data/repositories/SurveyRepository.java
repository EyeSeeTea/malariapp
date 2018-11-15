package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.usecase.LocalSurveyFilter;

import java.util.List;

public class SurveyRepository implements ISurveyRepository{

    private final ISurveyDataSource localDataSource;

    public SurveyRepository(
            ISurveyDataSource localDataSource) {
        this.localDataSource = localDataSource;
    }

    @Override
    public List<Survey> getSurveys(LocalSurveyFilter surveyStatus) throws Exception {
        return localDataSource.getSurveys(surveyStatus);
    }

    @Override
    public void save(List<Survey> surveys) throws Exception {
        localDataSource.save(surveys);
    }
}
