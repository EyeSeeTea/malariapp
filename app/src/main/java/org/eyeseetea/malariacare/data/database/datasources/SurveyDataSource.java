package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB_Table;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB_Table;
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
    public ArrayList<Survey> getQuarantineSurveysByProgramAndOrgUnit(String programUID,
            String orgUnitUID) {
        return new ArrayList<>(mapSurveyList(new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(Constants.SURVEY_QUARANTINE))
                .and(SurveyDB_Table.id_program_fk.eq(getProgramIdByUID(programUID)))
                .and(SurveyDB_Table.id_org_unit_fk.eq(getOrgUnitIdByUID(orgUnitUID)))
                .orderBy(OrderBy.fromProperty(
                        SurveyDB_Table.completion_date).descending()).queryList()));
    }

    @Override
    public Survey getMinQuarantineCompletionDateByProgramAndOrgUnit(String programUID,
            String orgUnitUID) {
        SurveyDB survey = new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(Constants.SURVEY_QUARANTINE))
                .and(SurveyDB_Table.id_program_fk.eq(getProgramIdByUID(programUID)))
                .and(SurveyDB_Table.id_org_unit_fk.eq(getOrgUnitIdByUID(orgUnitUID)))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date).ascending())
                .querySingle();
        return mapDBSurveyToDomain(survey);
    }

    @Override
    public Survey getMaxQuarantineUpdatedDateByProgramAndOrgUnit(String programUID,
            String orgUnitUID) {
        SurveyDB survey = new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(Constants.SURVEY_QUARANTINE))
                .and(SurveyDB_Table.id_program_fk.eq(getProgramIdByUID(programUID)))
                .and(SurveyDB_Table.id_org_unit_fk.eq(getOrgUnitIdByUID(orgUnitUID)))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.upload_date).descending())
                .querySingle();
        return mapDBSurveyToDomain(survey);
    }


    @Override
    public List<Survey> getQuarantineSurveys() {
        List<SurveyDB> surveyDBS = new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(Constants.SURVEY_QUARANTINE))
                .queryList();
        return mapSurveyList(surveyDBS);
    }

    @Override
    public void saveOldSurvey(Survey survey) {
        mapOldSurveyToSurveyDB(survey).save();
    }


    private SurveyDB mapOldSurveyToSurveyDB(Survey survey) {
        SurveyDB surveyDB = new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.uid_event_fk.eq(survey.getUId()))
                .querySingle();
        surveyDB.setCompletionDate(survey.getCompletionDate());
        surveyDB.setEventUid(survey.getUId());
        surveyDB.setMainScore(survey.getScore().getScore());
        surveyDB.setScheduledDate(survey.getScheduledDate());
        surveyDB.setStatus(mapStatus(survey.getStatus()));
        surveyDB.setUploadDate(survey.getUpdateDate());
        return surveyDB;
    }


    private long getProgramIdByUID(String programUID) {
        ProgramDB programDB = new Select()
                .from(ProgramDB.class)
                .where(ProgramDB_Table.uid_program.eq(programUID))
                .querySingle();
        return programDB.getId_program();
    }

    private long getOrgUnitIdByUID(String orgUnitUID) {
        OrgUnitDB orgUnitDB = new Select()
                .from(OrgUnitDB.class)
                .where(OrgUnitDB_Table.uid_org_unit.eq(orgUnitUID))
                .querySingle();
        return orgUnitDB.getId_org_unit();
    }


    private List<Survey> mapSurveyList(List<SurveyDB> surveyDBsList) {
        List<Survey> surveys = new ArrayList<>();
        for (SurveyDB surveyDB : surveyDBsList) {
            surveys.add(mapDBSurveyToDomain(surveyDB));
        }
        return surveys;
    }

    private Survey mapDBSurveyToDomain(SurveyDB surveyDB) {
        return Survey.createExistedSurvey(surveyDB.getEventUid(), surveyDB.getProgram().getUid(),
                surveyDB.getOrgUnit().getUid(), surveyDB.getUser().getUid(),
                surveyDB.getCreationDate(), surveyDB.getUploadDate(), surveyDB.getScheduledDate(),
                surveyDB.getCompletionDate(), mapValuesDBToQuestionValue(surveyDB.getValues()),
                new Score(surveyDB.getEventUid(), surveyDB.getMainScore()),
                mapStatus(surveyDB.getStatus()));
    }


    private Survey.Status mapStatus(int status) {
        switch (status) {
            case Constants.SURVEY_COMPLETED:
                return Survey.Status.COMPLETED;
            case Constants.SURVEY_CONFLICT:
                return Survey.Status.CONFLICT;
            case Constants.SURVEY_IN_PROGRESS:
                return Survey.Status.IN_PROGRESS;
            case Constants.SURVEY_SENDING:
                return Survey.Status.SENDING;
            case Constants.SURVEY_PLANNED:
                return Survey.Status.PLANNED;
            case Constants.SURVEY_QUARANTINE:
                return Survey.Status.QUARANTINE;
            case Constants.SURVEY_SENT:
                return Survey.Status.QUARANTINE;
            default:
                return Survey.Status.COMPLETED;
        }
    }

    private int mapStatus(Survey.Status status) {
        switch (status) {
            case COMPLETED:
                return Constants.SURVEY_COMPLETED;
            case CONFLICT:
                return Constants.SURVEY_CONFLICT;
            case IN_PROGRESS:
                return Constants.SURVEY_IN_PROGRESS;
            case SENDING:
                return Constants.SURVEY_SENDING;
            case PLANNED:
                return Constants.SURVEY_PLANNED;
            case QUARANTINE:
                return Constants.SURVEY_QUARANTINE;
            case SENT:
                return Constants.SURVEY_SENT;
            default:
                return Constants.SURVEY_COMPLETED;
        }
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
