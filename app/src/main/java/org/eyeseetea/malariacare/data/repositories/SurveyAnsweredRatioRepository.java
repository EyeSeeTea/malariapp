package org.eyeseetea.malariacare.data.repositories;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatioDB;
import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatioDB_Table;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;


public class SurveyAnsweredRatioRepository implements ISurveyAnsweredRatioRepository {
    @Override
    public void saveSurveyAnsweredRatio(SurveyAnsweredRatio surveyAnsweredRatio) {
        fromEntityToModel(surveyAnsweredRatio).save();
    }

    @Override
    public SurveyAnsweredRatio getSurveyAnsweredRatioBySurveyId(long id_survey) {
        return getModelToEntity(getSurveyAnsweredRatio(id_survey));
    }

    private SurveyAnsweredRatioDB getSurveyAnsweredRatio(long idSurvey) {
        return new Select().from(SurveyAnsweredRatioDB.class)
                .where(SurveyAnsweredRatioDB_Table.id_survey.is(idSurvey)).querySingle();
    }

    private SurveyAnsweredRatio getModelToEntity(SurveyAnsweredRatioDB surveyAnsweredRatioDB) {
        if (surveyAnsweredRatioDB == null) {
            return null;
        }
        return new SurveyAnsweredRatio(surveyAnsweredRatioDB.getIdSurvey(),
                surveyAnsweredRatioDB.getTotalQuestions(),
                surveyAnsweredRatioDB.getAnsweredQuestions(),
                surveyAnsweredRatioDB.getTotalCompulsoryQuestions(),
                surveyAnsweredRatioDB.getAnsweredCompulsoryQuestions());
    }

    private SurveyAnsweredRatioDB fromEntityToModel(SurveyAnsweredRatio surveyAnsweredRatio) {
        SurveyAnsweredRatioDB surveyAnsweredRatioDB = getSurveyAnsweredRatio(
                surveyAnsweredRatio.getSurveyId());
        if (surveyAnsweredRatioDB == null) {
            surveyAnsweredRatioDB = new SurveyAnsweredRatioDB();
        }
        surveyAnsweredRatioDB.setIdSurvey(surveyAnsweredRatio.getSurveyId());
        surveyAnsweredRatioDB.setTotalQuestions(surveyAnsweredRatio.getTotal());
        surveyAnsweredRatioDB.setAnsweredQuestions(surveyAnsweredRatio.getAnswered());
        surveyAnsweredRatioDB.setTotalCompulsoryQuestions(surveyAnsweredRatio.getTotalCompulsory());
        surveyAnsweredRatioDB.setAnsweredCompulsoryQuestions(
                surveyAnsweredRatio.getCompulsoryAnswered());
        return surveyAnsweredRatioDB;
    }
}
