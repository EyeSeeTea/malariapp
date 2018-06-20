package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB_Table;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.QuestionValue;
import org.eyeseetea.malariacare.domain.entity.Score;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SurveyDataSource implements ISurveyRepository {
    @Override
    public ArrayList<Survey> getQuarantineSurveysByProgramAndOrgUnit(String uidProgram,
            String uidOrgUnit) {
        return mapDBSurveyToDomain(new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(Constants.SURVEY_QUARANTINE))
                .and(SurveyDB_Table.id_program_fk.eq(program.getId_program()))
                .and(SurveyDB_Table.id_org_unit_fk.eq(orgUnit.getId_org_unit()))
                .orderBy(OrderBy.fromProperty(
                        SurveyDB_Table.completion_date).descending()).queryList());
    }

    private Survey mapDBSurveyToDomain(SurveyDB surveyDB) {
        return Survey.createExistedSurvey(surveyDB.getEventUid(), surveyDB.getProgram().getUid(),
                surveyDB.getOrgUnit().getUid(), surveyDB.getUser().getUid(),
                surveyDB.getCreationDate(), surveyDB.getUploadDate(), surveyDB.getScheduledDate(),
                surveyDB.getCompletionDate(), mapValuesDBToQuestionValue(surveyDB.getValues()),
                new Score(surveyDB.getEventUid(), surveyDB.getMainScore()));
    }

    private List<QuestionValue> mapValuesDBToQuestionValue(List<ValueDB> valueDBS) {
        List<QuestionValue> questionValues = new ArrayList<>();
        for (ValueDB valueDB : valueDBS) {
            QuestionValue questionValue;
            if (valueDB.getOption() != null) {
                questionValue = QuestionValue.createOptionValue(valueDB.getQuestion().getUid(),
                        valueDB.getOption().getUid(), valueDB.getValue());
            } else {
                questionValue = QuestionValue.createSimpleValue(valueDB.getQuestion().getUid(),
                        valueDB.getValue());
            }
            questionValues.add(questionValue);
        }
        return questionValues;
    }
}
