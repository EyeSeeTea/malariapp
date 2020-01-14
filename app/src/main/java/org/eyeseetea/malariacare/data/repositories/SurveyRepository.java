package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SurveyRepository implements ISurveyRepository{

    private final ISurveyDataSource localDataSource;

    public SurveyRepository(
            ISurveyDataSource localDataSource) {
        this.localDataSource = localDataSource;
    }

    @Override
    public List<Survey> getSurveysByUIds(List<String> uids) throws Exception {
        return localDataSource.getSurveysByUids(uids);
    }

    @Override
    public void save(@NotNull List<Survey> surveys) throws Exception {
        localDataSource.save(surveys);
    }

    @NotNull
    @Override
    public Survey getSurveyByUid(@NotNull String uid) throws Exception {
        return localDataSource.getSurveyByUid(uid);
    }




}
