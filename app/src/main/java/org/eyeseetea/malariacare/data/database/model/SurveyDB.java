/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.model;

import static org.eyeseetea.malariacare.data.database.AppDatabase.matchAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.matchName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.orgUnitAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.orgUnitName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionOptionAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionOptionName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionRelationAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionRelationName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.surveyAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.surveyName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.valueAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.valueName;
import static org.eyeseetea.malariacare.utils.Constants.SURVEY_CONFLICT;
import static org.eyeseetea.malariacare.utils.Constants.SURVEY_QUARANTINE;
import static org.eyeseetea.malariacare.utils.Constants.SURVEY_SENT;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.VisitableToSDK;
import org.eyeseetea.malariacare.data.sync.IData;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow_Table;
import org.hisp.dhis.client.sdk.core.common.utils.CodeGenerator;

import java.util.Date;
import java.util.List;

@Table(database = AppDatabase.class, name = "Survey")
public class SurveyDB extends BaseModel implements VisitableToSDK, IData {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_survey;

    @Column
    Long id_program_fk;
    /**
     * Reference to the program associated to this survey (loaded lazily)
     */
    ProgramDB program;

    @Column
    Long id_org_unit_fk;
    /**
     * Reference to the org unit associated to this survey (loaded lazily)
     */
    OrgUnitDB orgUnit;

    @Column
    Long id_user_fk;
    /**
     * Reference to the user that has created this survey (loaded lazily)
     */
    UserDB user;

    @Column
    Date creation_date;

    @Column
    Date completion_date;

    @Column
    Date upload_date;

    @Column
    Date scheduled_date;

    @Column
    Integer status;

    @Column
    String uid_event_fk;

    @Column
    Integer competency_score_classification = 0;

    /**
     * List of values for this survey
     */
    List<ValueDB> values;

    /**
     * List of historic previous schedules
     */
    List<SurveyScheduleDB> surveySchedules;

    /**
     * Calculated main Score for this survey, is not persisted, just calculated on runtime
     */
    Float mainScore;

    /**
     * hasMainScore is used to know if the survey have a compositeScore with only 1 query time.
     */
    private Boolean hasMainScore = null;

    /**
     * Expected productivity for this survey according to its orgunit + program.
     * Just a cached value from orgunitprogramproductivity
     */
    Integer productivity;

    private SurveyAnsweredRatioDB mSurveyAnsweredRatio;
    private ScoreDB scoreDB;

    public SurveyDB() {
        //Set dates
        this.creation_date = new Date();
        this.completion_date = null;
        this.upload_date = null;
        this.scheduled_date = null;

        //// TODO: 26/07/2018  This action should be make on survey entity creation (SurveyPlanned build or when create a new survey).
        this.uid_event_fk = CodeGenerator.generateCode();
    }

    public SurveyDB(OrgUnitDB orgUnit, ProgramDB program, UserDB user) {
        this();

        // Possibilities [ In progress | Completed | Sent ]
        this.status = Constants.SURVEY_IN_PROGRESS;

        //Set context of the survey
        this.setOrgUnit(orgUnit);
        this.setProgram(program);
        this.setUser(user);
    }

    public Long getId_survey() {
        return id_survey;
    }

    public void setId_survey(Long id_survey) {
        this.id_survey = id_survey;
    }

    public String getEventUid() {
        return uid_event_fk;
    }

    public void setEventUid(String eventuid) {
        this.uid_event_fk = eventuid;
    }

    public Long getId_program_fk() {
        return id_program_fk;
    }

    public Long getId_org_unit_fk() {
        return id_org_unit_fk;
    }

    public Long getId_user_fk() {
        return id_user_fk;
    }

    public OrgUnitDB getOrgUnit() {
        if (orgUnit == null) {
            if (id_org_unit_fk == null) return null;
            orgUnit = new Select().from(OrgUnitDB.class)
                    .where(OrgUnitDB_Table.id_org_unit
                            .is(id_org_unit_fk)).querySingle();
        }
        return orgUnit;
    }

    public void setOrgUnit(OrgUnitDB orgUnit) {
        this.orgUnit = orgUnit;
        this.id_org_unit_fk = (orgUnit != null) ? orgUnit.getId_org_unit() : null;
    }

    public void setOrgUnit(Long id_org_unit) {
        this.id_org_unit_fk = id_org_unit;
        this.orgUnit = null;
    }

    public ProgramDB getProgram() {
        if (program == null) {
            if (id_program_fk == null) return null;
            program = new Select()
                    .from(ProgramDB.class)
                    .where(ProgramDB_Table.id_program
                            .is(id_program_fk)).querySingle();
        }
        return program;
    }

    public void setProgram(ProgramDB program) {
        this.program = program;
        this.id_program_fk = (program != null) ? program.getId_program() : null;
    }

    public void setProgram(Long id_program) {
        this.id_program_fk = id_program;
        this.program = null;
    }

    public UserDB getUser() {
        if (user == null) {
            if (id_user_fk == null) return null;
            user = new Select()
                    .from(UserDB.class)
                    .where(UserDB_Table.id_user
                            .is(id_user_fk)).querySingle();
        }
        return user;
    }

    public void setUser(UserDB user) {
        this.user = user;
        this.id_user_fk = (user != null) ? user.getId_user() : null;
    }

    public void setUser(Long id_user) {
        this.id_user_fk = id_user;
        this.user = null;
    }

    public Date getCreationDate() {
        return creation_date;
    }

    public void setCreationDate(Date creation_date) {
        this.creation_date = creation_date;
    }

    public Date getCompletionDate() {
        return completion_date;
    }

    public void setCompletionDate(Date completion_date) {
        this.completion_date = completion_date;
    }

    public Date getUploadDate() {
        return upload_date;
    }

    public void setUploadDate(Date upload_date) {
        this.upload_date = upload_date;
    }

    public Date getScheduledDate() {
        return scheduled_date;
    }

    public void setScheduledDate(Date scheduled_date) {
        this.scheduled_date = scheduled_date;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        Log.d(SurveyDB.class.getName() + "B&D",
                "Id: " + getId_survey() + " actual status:" + status + " set as:" + status);
        this.status = status;
    }

    public Integer getCompetencyScoreClassification() {
        return competency_score_classification;
    }

    public void setCompetencyScoreClassification(
            Integer competencyScoreClassification) {
        this.competency_score_classification = competencyScoreClassification;
    }

    /**
     * Checks if the survey has been sent or not
     *
     * @return true|false
     */
    public boolean isSent() {
        return SURVEY_SENT == this.status;
    }

    /**
     * Checks if the survey is PLANNED
     *
     * @return true|false
     */
    public boolean isPlanned() {
        return Constants.SURVEY_PLANNED == this.status;
    }

    /**
     * Checks if the survey is IN_PROGRESS
     *
     * @return true|false
     */
    public boolean isInProgress() {
        return Constants.SURVEY_IN_PROGRESS == this.status;
    }

    /**
     * Checks if the survey is Quarantine
     *
     * @return true|false
     */
    public boolean isInQuarantine() {
        return SURVEY_QUARANTINE == this.status;
    }

    /**
     * Checks if the survey has been completed or not
     *
     * @return true|false
     */
    public boolean isCompleted() {
        return Constants.SURVEY_COMPLETED == this.status;
    }

    public boolean isConflict() {
        return SURVEY_CONFLICT == this.status;
    }

    public boolean isReadOnly() {
        return (isCompleted() || isSent());
    }

    public Float getMainScoreValue() {
        float score = 0;

        if (getMainScore() != null)
            score = getMainScore().getScore();

        return score;
    }

    public ScoreDB getMainScore() {
        //The main score is only return from a query 1 time
        if (this.scoreDB == null) {
            scoreDB = getScore();
        }
        return scoreDB;
    }

    public Boolean hasMainScore() {
        if (hasMainScore == null || !hasMainScore) {
            ScoreDB score = getMainScore();
            Float value = (score == null) ? null : score.getScore();
            if (value == null) {
                hasMainScore = false;
            } else {
                hasMainScore = true;
            }
        }
        return hasMainScore;
    }

    public void setMainScore(long survey_id, String compositeScoreUid, Float mainScore) {
        scoreDB = new ScoreDB(survey_id, compositeScoreUid, mainScore);
    }

    public void saveMainScore() {
        if (scoreDB == null) {

            scoreDB = getScore();

            if (scoreDB == null) {
                calculateScore(this.getClass().getSimpleName());
            }
        }

        scoreDB.save();
    }

    private ScoreDB getScore() {
        return new Select()
                .from(ScoreDB.class)
                .where(ScoreDB_Table.id_survey_fk.eq(this.getId_survey())).querySingle();
    }

    public ScoreDB getScoreDB() {
        return scoreDB;
    }

    public void setScoreDB(ScoreDB scoreDB) {
        this.scoreDB = scoreDB;
    }

    /**
     * Returns the productivity for this survey according to its orgunit + program
     */
    public Integer getProductivity() {
        if (productivity == null) {
            productivity = OrgUnitProgramRelationDB.getProductivity(this);
        }
        return productivity;
    }

    /**
     * Returns if this survey has low productivity or not.
     * [0..4]: Low
     * [5..): Not Low
     */
    public boolean isLowProductivity() {
        return getProductivity() < 5;
    }

    @Override
    public void delete() {
        ScoreDB score = getScore();
        if (score != null) {
            score.delete();
        }
        super.delete();
    }

    /**
     * Returns the list of answered values from this survey
     */
    public List<ValueDB> getValues() {
        if (values == null) {
            values = new Select()
                    .from(ValueDB.class)
                    .where(ValueDB_Table.id_survey_fk
                            .eq(this.getId_survey())).queryList();
        }
        return values;
    }

    public void setValues(List<ValueDB> values) {
        this.values = values;
    }

    /**
     * Returns the list of previous schedules for this survey
     */
    public List<SurveyScheduleDB> getSurveySchedules() {
        if (surveySchedules == null) {
            surveySchedules = new Select()
                    .from(SurveyScheduleDB.class)
                    .where(SurveyScheduleDB_Table.id_survey_fk
                            .eq(this.getId_survey())).queryList();
        }
        return surveySchedules;
    }

    /**
     * Update the SurveySchedule to be set in a new survey, and could remove the old survey without
     * lost that
     *
     * @param newSurvey to set the SurveSchedule
     */
    public void setSurveyScheduleToSurvey(SurveyDB newSurvey) {
        Where update = new Update<>(SurveyScheduleDB.class).set(
                SurveyScheduleDB_Table.id_survey_fk.eq(newSurvey.getId_survey()))
                .where(SurveyScheduleDB_Table.id_survey_fk.is(id_survey));
        //old update.queryClose();
        update.query();
    }

    /**
     * Return the number of child questions that should be answered according to the values of the
     * parent questions.
     */
    public long countNumOptionalQuestionsToAnswer() {
        long numOptionalQuestions = SQLite.selectCountOf().from(QuestionDB.class).as(questionName)
                .join(QuestionRelationDB.class, Join.JoinType.LEFT_OUTER).as(questionRelationName)
                .on(QuestionDB_Table.id_question.withTable(questionAlias)
                        .eq(QuestionRelationDB_Table.id_question_fk.withTable(
                                questionRelationAlias)))
                .join(MatchDB.class, Join.JoinType.LEFT_OUTER).as(matchName)
                .on(QuestionRelationDB_Table.id_question_relation.withTable(questionRelationAlias)
                        .eq(MatchDB_Table.id_question_relation_fk.withTable(matchAlias)))
                .join(QuestionOptionDB.class, Join.JoinType.LEFT_OUTER).as(questionOptionName)
                .on(MatchDB_Table.id_match.withTable(matchAlias)
                        .eq(QuestionOptionDB_Table.id_match_fk.withTable(questionOptionAlias)))
                .join(ValueDB.class, Join.JoinType.LEFT_OUTER).as(valueName)
                .on(ValueDB_Table.id_question_fk.withTable(valueAlias)
                                .eq(QuestionOptionDB_Table.id_question_fk.withTable(questionOptionAlias)),
                        ValueDB_Table.id_option_fk.withTable(valueAlias)
                                .eq(QuestionOptionDB_Table.id_option_fk.withTable(
                                        questionOptionAlias)))
                //Parent Child relationship
                .where(QuestionRelationDB_Table.operation.withTable(questionRelationAlias).eq(
                        QuestionRelationDB.PARENT_CHILD))
                //For the given survey
                .and(ValueDB_Table.id_survey_fk.withTable(valueAlias).eq(this.getId_survey()))
                //The child question requires an answer
                .and(QuestionDB_Table.output.withTable(questionAlias).isNot(Constants.NO_ANSWER))
                .count();
        //Parent with the right value -> not hidden
        return numOptionalQuestions;
    }

    /**
     * Updates ratios, status and completion date depending on the question and answer (text)
     */
    public void updateSurveyStatus(SurveyAnsweredRatio surveyAnsweredRatio) {

        //Exit if the survey was sent or completed
        if (isReadOnly()) {
            return;
        }

        if (surveyAnsweredRatio.getTotalCompulsory() == 0) {
            //Update status
            if (!surveyAnsweredRatio.isCompleted()) {
                setStatus(Constants.SURVEY_IN_PROGRESS);
            }

        } else if (surveyAnsweredRatio.getCompulsoryAnswered() == 0) {
            setStatus(Constants.SURVEY_IN_PROGRESS);
        }


        //Saves new status & completion_date
        save();
    }

    public void saveScore(String module) {        //Prepare scores info
        calculateScore(module);

        this.saveMainScore();
    }

    private void calculateScore(String module) {
        List<CompositeScoreDB> compositeScoreList = ScoreRegister.loadCompositeScores(this, module);

        //Calculate main score to push later

        this.setMainScore(id_survey,
                ScoreRegister.getCompositeScoreRoot(compositeScoreList).getUid(),
                ScoreRegister.calculateMainScore(compositeScoreList, id_survey, module));
    }

    /**
     * Returns a survey in progress for the given orgUnit and program
     */
    public static SurveyDB getInProgressSurveys(OrgUnitDB orgUnit, ProgramDB program) {
        if (orgUnit == null || program == null) {
            return null;
        }

        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.id_org_unit_fk.eq(orgUnit.getId_org_unit()))
                .and(SurveyDB_Table.id_program_fk.eq(program.getId_program()))
                .and(SurveyDB_Table.status.is(Constants.SURVEY_IN_PROGRESS))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).querySingle();
    }

    /**
     * Returns the number of surveys with status yet not put to "Sent/conflict/planned.."
     */
    public static int countAllUnsentUnplannedSurveys() {
        return (int) SQLite.selectCountOf().from(SurveyDB.class)
                .where(SurveyDB_Table.status.is(Constants.SURVEY_COMPLETED))
                .or(SurveyDB_Table.status.is(Constants.SURVEY_IN_PROGRESS))
                .or(SurveyDB_Table.status.is(Constants.SURVEY_SENDING))
                .or(SurveyDB_Table.status.is(SURVEY_QUARANTINE))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).count();
    }

    /**
     * Returns all the surveys with status yet not put to "Sent"
     */
    public static List<SurveyDB> getAllUnsentUnplannedSurveys() {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.is(Constants.SURVEY_COMPLETED))
                .or(SurveyDB_Table.status.is(Constants.SURVEY_IN_PROGRESS))
                .or(SurveyDB_Table.status.is(Constants.SURVEY_SENDING))
                .or(SurveyDB_Table.status.is(SURVEY_QUARANTINE))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();
    }

    /**
     * Returns the last surveys (by date) with status yet not put to "Sent"
     */
    public static List<SurveyDB> getUnsentSurveys(int limit) {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.isNot(SURVEY_SENT))
                .limit(limit)
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();
    }

    /**
     * Returns all the surveys with status put to "Sent"
     */
    public static List<SurveyDB> getAllSentSurveys() {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(SURVEY_SENT))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();
    }

    /**
     * Returns the last surveys (by date) with status put to "Sent"
     */
    public static List<SurveyDB> getSentSurveys(int limit) {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(SURVEY_SENT))
                .limit(limit)
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();
    }

    /**
     * Returns all the surveys with status put to "Completed"
     */
    public static List<SurveyDB> getAllCompletedSurveys() {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(Constants.SURVEY_COMPLETED))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();
    }

    public static List<SurveyDB> getAllSendingSurveys() {
        return new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(Constants.SURVEY_SENDING))
                .queryList();
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) throws ConversionException {
        IConvertToSDKVisitor.visit(this);
    }

    /* Returns the last surveys (by date) with status Completed or sent
     * @return
     */
    public static List<SurveyDB> getAllCompletedUnsentSurveys() {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.is(Constants.SURVEY_COMPLETED))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();
    }

    /**
     * Returns the last surveys (by date) without status Completed or sent
     */
    public static List<SurveyDB> getAllInProgressSurveys() {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.is(Constants.SURVEY_IN_PROGRESS))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();
    }

    /**
     * Returns the last survey by each program and orgunit combination ordered by completiondate
     */
    public static List<SurveyDB> getLastSentCompletedOrConflictSurveys() {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(SURVEY_SENT))
                .or(SurveyDB_Table.status.eq(Constants.SURVEY_COMPLETED))
                .or(SurveyDB_Table.status.eq(SURVEY_CONFLICT))
                .or(SurveyDB_Table.status.eq(SURVEY_QUARANTINE))
                .or(SurveyDB_Table.status.eq(Constants.SURVEY_SENDING))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .groupBy(SurveyDB_Table.id_org_unit_fk, SurveyDB_Table.id_program_fk)
                .having(SurveyDB_Table.completion_date.eq(
                        Method.max(SurveyDB_Table.completion_date)))
                .queryList();
    }

    public static List<SurveyDB> getAllSentCompletedOrConflictSurveysAfterDate(Date afterDate) {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.isNot(Constants.SURVEY_HIDE))
                .and(SurveyDB_Table.status.isNot(Constants.SURVEY_IN_PROGRESS))
                .and(SurveyDB_Table.completion_date.greaterThan(afterDate))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date)).queryList();
    }

    /**
     * Returns the last survey by each program and orgunit combination ordered by completiondate
     */
    public static List<SurveyDB> getLastSentSurveysByProgramAndOrgUnit(
            String programUid, String orgUnitUid) {

        ConditionGroup conditionHavingGroup = ConditionGroup.clause().and(
                SurveyDB_Table.completion_date.eq(Method.max(SurveyDB_Table.completion_date)));

        if (programUid != null && programUid.isEmpty() == false) {
            conditionHavingGroup.and(
                    ProgramDB_Table.uid_program.withTable(programAlias).eq(programUid));
        }

        if (orgUnitUid != null && orgUnitUid.isEmpty() == false) {
            conditionHavingGroup.and(
                    OrgUnitDB_Table.uid_org_unit.withTable(orgUnitAlias).eq(orgUnitUid));
        }


        BaseModelQueriable<SurveyDB> query = new Select().from(SurveyDB.class).as(surveyName)
                .join(ProgramDB.class, Join.JoinType.LEFT_OUTER).as(programName)
                .on(SurveyDB_Table.id_program_fk.withTable(surveyAlias)
                        .eq(ProgramDB_Table.id_program.withTable(programAlias)))
                .join(OrgUnitDB.class, Join.JoinType.LEFT_OUTER).as(orgUnitName)
                .on(SurveyDB_Table.id_org_unit_fk.withTable(surveyAlias)
                        .eq(OrgUnitDB_Table.id_org_unit.withTable(orgUnitAlias)))
                .where(SurveyDB_Table.status.withTable(surveyAlias).eq(SURVEY_SENT))
                .or(SurveyDB_Table.status.withTable(surveyAlias).eq(Constants.SURVEY_COMPLETED))
                .or(SurveyDB_Table.status.withTable(surveyAlias).eq(SURVEY_CONFLICT))
                .or(SurveyDB_Table.status.withTable(surveyAlias).eq(SURVEY_QUARANTINE))
                .or(SurveyDB_Table.status.withTable(surveyAlias).eq(Constants.SURVEY_SENDING))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .groupBy(SurveyDB_Table.id_org_unit_fk, SurveyDB_Table.id_program_fk)
                .having(conditionHavingGroup);

        Log.d("", query.toString());

        return query.queryList();
    }

    /**
     * Returns all the surveys with status put to "Sent" or completed or Conflict
     */
    public static List<SurveyDB> getAllSentCompletedOrConflictSurveys() {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(SURVEY_SENT))
                .or(SurveyDB_Table.status.eq(Constants.SURVEY_COMPLETED))
                .or(SurveyDB_Table.status.eq(SURVEY_CONFLICT))
                .or(SurveyDB_Table.status.eq(SURVEY_QUARANTINE))
                .or(SurveyDB_Table.status.eq(Constants.SURVEY_SENDING))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date)).queryList();
    }


    public static List<SurveyDB> getAllQuarantineSurveysByProgramAndOrgUnit(ProgramDB program,
            OrgUnitDB orgUnit) {
        return new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(SURVEY_QUARANTINE))
                .and(SurveyDB_Table.id_program_fk.eq(program.getId_program()))
                .and(SurveyDB_Table.id_org_unit_fk.eq(orgUnit.getId_org_unit()))
                .orderBy(OrderBy.fromProperty(
                        SurveyDB_Table.completion_date).descending()).queryList();
    }

    public static Date getMinQuarantineCompletionDateByProgramAndOrgUnit(ProgramDB program,
            OrgUnitDB orgUnit) {
        SurveyDB survey = new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(SURVEY_QUARANTINE))
                .and(SurveyDB_Table.id_program_fk.eq(program.getId_program()))
                .and(SurveyDB_Table.id_org_unit_fk.eq(orgUnit.getId_org_unit()))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date).ascending())
                .querySingle();
        return survey.getCompletionDate();
    }

    public static Date getMaxQuarantineUpdatedDateByProgramAndOrgUnit(ProgramDB program,
            OrgUnitDB orgUnit) {
        SurveyDB survey = new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(SURVEY_QUARANTINE))
                .and(SurveyDB_Table.id_program_fk.eq(program.getId_program()))
                .and(SurveyDB_Table.id_org_unit_fk.eq(orgUnit.getId_org_unit()))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.upload_date).descending())
                .querySingle();
        return survey.getUploadDate();
    }

    /**
     * Returns all the surveys with status put to "quarantine"
     */
    public static int countQuarantineSurveys() {
        return (int) SQLite.selectCountOf()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(SURVEY_QUARANTINE))
                .count();
    }

    public static long count() {
        return SQLite.selectCountOf()
                .from(SurveyDB.class)
                .count();
    }

    public boolean hasConflict() {
        for (ValueDB value : getValues()) {
            if (value.getConflict()) {
                return true;
            }
        }
        return false;
    }

    public void prepareSurveyUploadedDate() {
        if (!isSent()) {
            setUploadDate(new Date());
            save();
        }
    }

    public void setSentSurveyState() {
        //Change status and save mainScore
        setStatus(SURVEY_SENT);
        save();
        saveMainScore();
    }

    public void setCompleteSurveyState() {
        setStatus(Constants.SURVEY_COMPLETED);
    }

    /**
     * Moves the scheduled date for this survey to a new given date due to a given reason (comment)
     */
    public void reschedule(Date newScheduledDate, String comment) {
        //Take currentDate
        Date currentScheduledDate = this.getScheduledDate();

        //Add a history
        SurveyScheduleDB previousSchedule = new SurveyScheduleDB(this, currentScheduledDate,
                comment);
        previousSchedule.save();

        //Clean inner lazy schedulelist
        surveySchedules = null;

        //Move scheduledate and save
        this.scheduled_date = newScheduledDate;
        this.save();
    }

    /**
     * Finds a survey by its ID
     */
    public static SurveyDB findById(Long id_survey) {
        return new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.id_survey
                        .eq(id_survey))
                .querySingle();
    }

    public static boolean exists(Long id_survey) {
        return findById(id_survey) != null;
    }

    /**
     * Returns all surveys which status is 'planned' or 'in progress'
     */
    public static List<SurveyDB> findPlannedOrInProgress() {
        return new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.status.eq(Constants.SURVEY_PLANNED))
                .or(SurveyDB_Table.status.eq(Constants.SURVEY_IN_PROGRESS))
                .orderBy(SurveyDB_Table.scheduled_date, true)
                .queryList();
    }

    /**
     * Finds a survey with a given orgunit and program
     */
    public static SurveyDB findPlannedByOrgUnitAndProgram(OrgUnitDB orgUnit, ProgramDB program) {
        return new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.id_org_unit_fk.eq(orgUnit.getId_org_unit()))
                .and(SurveyDB_Table.id_program_fk.eq(program.getId_program()))
                .and(SurveyDB_Table.status.eq(Constants.SURVEY_PLANNED))
                .querySingle();
    }

    /**
     * Find the last survey that has been sent for each orgunit+program combination
     */
    public static List<SurveyDB> listLastByOrgUnitProgram() {
        return SQLite.select()
                .from(SurveyDB.class)
                .where()
                .groupBy(SurveyDB_Table.id_org_unit_fk, SurveyDB_Table.id_program_fk)
                .having(SurveyDB_Table.completion_date.eq(
                        Method.max(SurveyDB_Table.completion_date)))
                .queryList();
    }

    public static SurveyDB getLastSurvey(Long id_org_unit, Long id_program) {
        return SQLite.select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.id_program_fk.eq(id_program))
                .and(SurveyDB_Table.id_org_unit_fk.eq(id_org_unit))
                .groupBy(SurveyDB_Table.id_program_fk, SurveyDB_Table.id_org_unit_fk)
                .having(SurveyDB_Table.completion_date.eq(
                        Method.max(SurveyDB_Table.completion_date)))
                .querySingle();
    }

    /**
     * Get event from a survey if exists.
     */
    public EventFlow getEvent() {
        return new Select().from(EventFlow.class)
                .where(EventFlow_Table.uId.eq(uid_event_fk)).querySingle();
    }

    /**
     * Get event from a survey local id if exist
     */
    public EventFlow getEventFromLocalId() {
        EventFlow event = new Select().from(EventFlow.class)
                .where(EventFlow_Table.id.eq(id_survey)).querySingle();
        return event;
    }

    public static SurveyDB getSurveyInProgress() {
        return new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.status
                        .eq(Constants.SURVEY_IN_PROGRESS))
                .querySingle();
    }

    public static SurveyDB getSurveyById(long id) {
        return new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.id_survey.eq(id)).querySingle();
    }

    public static SurveyDB getSurveyByUId(String uid) {
        return new Select()
                .from(SurveyDB.class)
                .where(SurveyDB_Table.uid_event_fk.eq(uid)).querySingle();
    }

    public String getFullName() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getOrgUnit().getName());
        stringBuilder.append(", ");
        stringBuilder.append(this.getProgram().getName());
        return stringBuilder.toString();
    }

    @Override
    public Long getSurveyId() {
        return getId_survey();
    }

    @Override
    public void changeStatusToSending() {
        setStatus(SurveyStatus.SENDING.getCode());
        save();
    }

    @Override
    public void changeStatusToSyncConversionError() {
        setStatus(SurveyStatus.ERROR_CONVERSION_SYNC.getCode());
        save();
    }

    @Override
    public void changeStatusToQuarantine() {
        setStatus(SurveyStatus.QUARANTINE.getCode());
        save();
    }

    @Override
    public void changeStatusToConflict() {
        setStatus(SurveyStatus.CONFLICT.getCode());
        save();
    }

    @Override
    public void changeStatusToSent() {
        setStatus(SurveyStatus.SENT.getCode());
        saveMainScore();
        save();
    }

    @Override
    public void saveConflict(String questionUid) {
        for (ValueDB value : getValues()) {
            if (value.getQuestion().getUid().equals(questionUid)) {
                value.setConflict(true);
                value.save();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurveyDB survey = (SurveyDB) o;

        if (id_survey != survey.id_survey) return false;
        if (id_program_fk != null ? !id_program_fk.equals(survey.id_program_fk)
                : survey.id_program_fk != null) {
            return false;
        }
        if (id_org_unit_fk != null ? !id_org_unit_fk.equals(survey.id_org_unit_fk)
                : survey.id_org_unit_fk != null) {
            return false;
        }
        if (id_user_fk != null ? !id_user_fk.equals(survey.id_user_fk)
                : survey.id_user_fk != null) {
            return false;
        }
        if (creation_date != null ? !creation_date.equals(survey.creation_date)
                : survey.creation_date != null) {
            return false;
        }
        if (completion_date != null ? !completion_date.equals(survey.completion_date)
                : survey.completion_date != null) {
            return false;
        }
        if (upload_date != null ? !upload_date.equals(survey.upload_date)
                : survey.upload_date != null) {
            return false;
        }
        if (uid_event_fk != null ? !uid_event_fk.equals(survey.uid_event_fk)
                : survey.uid_event_fk != null) {
            return false;
        }
        if (scheduled_date != null ? !scheduled_date.equals(survey.scheduled_date)
                : survey.scheduled_date != null) {
            return false;
        }
        return !(status != null ? !status.equals(survey.status) : survey.status != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_survey ^ (id_survey >>> 32));
        result = 31 * result + (id_program_fk != null ? id_program_fk.hashCode() : 0);
        result = 31 * result + (id_org_unit_fk != null ? id_org_unit_fk.hashCode() : 0);
        result = 31 * result + (id_user_fk != null ? id_user_fk.hashCode() : 0);
        result = 31 * result + (creation_date != null ? creation_date.hashCode() : 0);
        result = 31 * result + (completion_date != null ? completion_date.hashCode() : 0);
        result = 31 * result + (upload_date != null ? upload_date.hashCode() : 0);
        result = 31 * result + (uid_event_fk != null ? uid_event_fk.hashCode() : 0);
        result = 31 * result + (scheduled_date != null ? scheduled_date.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "id_survey=" + id_survey +
                ", id_program=" + id_program_fk +
                ", id_org_unit=" + id_org_unit_fk +
                ", id_user=" + id_user_fk +
                ", creation_date=" + creation_date +
                ", completion_date=" + completion_date +
                ", upload_date=" + upload_date +
                ", scheduled_date=" + scheduled_date +
                ", status=" + status +
                ", uid_event_fk=" + uid_event_fk +
                '}';
    }

    public void resetMainScore() {
        scoreDB = getMainScore();
        if(scoreDB!=null){
            scoreDB.delete();
        }
        scoreDB = null;
    }
}