package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;

import java.util.List;

public class SurveyLocalDataSource implements ISurveyDataSource{
    @Override
    public List<Survey> getSurveys(SurveyFilter filters) throws Exception {
        //On the future implement this method to retrieve surveys from db
        return null;
    }

    @Override
    public void Save(List<Survey> surveys) throws Exception {

    }
}
