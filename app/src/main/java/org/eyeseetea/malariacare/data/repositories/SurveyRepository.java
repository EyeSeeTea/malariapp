package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.SurveyFilter;

import java.util.List;

public class SurveyRepository implements ISurveyRepository{

    private final ISurveyDataSource localDataSource;
    private final ISurveyDataSource remoteApiDataSource;

    public SurveyRepository(
            ISurveyDataSource localDataSource,
            ISurveyDataSource remoteApiDataSource) {
        this.localDataSource = localDataSource;
        this.remoteApiDataSource = remoteApiDataSource;
    }

    @Override
    public List<Survey> getSurveys(SurveyFilter surveyStatus) throws Exception {
        if(surveyStatus.getReadPolicy().equals(ReadPolicy.CACHE)) {
            return localDataSource.getSurveys(surveyStatus);
        } else if (surveyStatus.getReadPolicy().equals(ReadPolicy.NETWORK_NO_CACHE)){
            return remoteApiDataSource.getSurveys(surveyStatus);
        }
        //// TODO: 27/11/2018  implements Network first 
        return null;
    }

    @Override
    public void save(List<Survey> surveys) throws Exception {
        localDataSource.save(surveys);
    }
}
