package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.Survey;

import java.util.List;

public class SurveyRepository implements ISurveyRepository{

    private final ISurveyDataSource localDataSource;

    public SurveyRepository(
            ISurveyDataSource localDataSource) {
        this.localDataSource = localDataSource;
    }

    @Override
    public List<Survey> getSurveysByUids(List<String> uids) throws Exception {
        return localDataSource.getSurveysByUids(uids);
    }

    @Override
    public void save(List<Survey> surveys) throws Exception {
        localDataSource.save(surveys);
    }
}
