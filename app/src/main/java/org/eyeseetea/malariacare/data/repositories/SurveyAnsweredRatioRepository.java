package org.eyeseetea.malariacare.data.repositories;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatioDB;
import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatioDB_Table;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.usecase.ISurveyAnsweredRatioCallback;

import java.util.List;


public class SurveyAnsweredRatioRepository implements ISurveyAnsweredRatioRepository {
    @Override
    public void saveSurveyAnsweredRatio(SurveyAnsweredRatio surveyAnsweredRatio) {
        fromEntityToModel(surveyAnsweredRatio).save();
    }

    @Override
    public SurveyAnsweredRatio getSurveyAnsweredRatioBySurveyId(long id_survey) {
        return getModelToEntity(getSurveyAnsweredRatio(id_survey));
    }

    @Override
    public SurveyAnsweredRatio getSurveyAnsweredRatioBySurveyUId(String surveyUId) {
        SurveyDB survey = SurveyDB.getSurveyByUId(surveyUId);

        return getModelToEntity(getSurveyAnsweredRatio(survey.getId_survey()));
    }

    @Override
    public SurveyAnsweredRatio loadSurveyAnsweredRatio(ISurveyAnsweredRatioCallback callback, SurveyDB surveyDB) {
        SurveyAnsweredRatio surveyAnsweredRatio =null;
        ProgramDB surveyProgram = surveyDB.getProgram();
        int numActiveTotalQuestions = QuestionDB.countActiveTotalQuestionsByProgram(surveyProgram);
        int numActiveTotalCompulsory = QuestionDB.countActiveTotalCompulsoryByProgram(surveyProgram);
        int numAnswered = ValueDB.countBySurvey(surveyDB);
        int numCompulsoryAnswered = ValueDB.countCompulsoryBySurvey(surveyDB);
        surveyAnsweredRatio = new SurveyAnsweredRatio(surveyDB.getId_survey(),
                numActiveTotalQuestions,
                numAnswered, numActiveTotalCompulsory,
                numCompulsoryAnswered);
        return surveyAnsweredRatio;
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
