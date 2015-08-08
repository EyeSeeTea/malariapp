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

package org.eyeseetea.malariacare.database.model;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatioCache;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.Date;
import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Survey extends BaseModel {


    /*private static final String LIST_VALUES_PARENT_QUESTION = "select v.* from value v"+
            " left join question q on v.question=q.id"+
            " where v.survey=?"+
            " and q.question=0"+
            " and v.value is not null and v.value<>''";*/

    @Column
    @PrimaryKey(autoincrement = true)
    long id;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_program",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    Program program;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_org_unit",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    OrgUnit orgUnit;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_user",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    User user;
    @Column
    Date eventDate;
    @Column
    Date completionDate;
    @Column
    Integer status;

    List<Value> values;

    SurveyAnsweredRatio answeredQuestionRatio;

    public Survey() {
    }

    public Survey(OrgUnit orgUnit, Program program, User user) {
        this.orgUnit = orgUnit;
        this.program = program;
        this.user = user;
        this.eventDate = new Date();
        this.status = Constants.SURVEY_IN_PROGRESS; // Possibilities [ In progress | Completed | Sent ]
        this.completionDate = this.eventDate;

        Log.i(".Survey", Long.valueOf(this.completionDate.getTime()).toString());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrgUnit getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit = orgUnit;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Date getCompletionDate(){
        return completionDate;
    }

    public void setCompletionDate(Date completionDate){
        this.completionDate=completionDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * Checks if the survey has been sent or not
     * @return true|false
     */
    public boolean isSent(){
        return Constants.SURVEY_SENT==this.status;
    }

    /**
     * Returns the list of answered values from this survey
     * @return
     */
    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "values")
    public List<Value> getValues(){
        return new Select().from(Value.class)
                .where(Condition.column(Value$Table.SURVEY_ID_SURVEY).eq(this.getId())).queryList();
    }

    /**
     * Returns the list of answered values from this survey that belong to a parent question
     * @return
     */
    public List<Value> getValuesFromParentQuestions(){
        List<Value> values = new Select().all().from(Value.class).as("v")
                .join(Question.class, Join.JoinType.LEFT).as("q")
                .on(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.QUESTION_ID_QUESTION))
                        .eq(ColumnAlias.columnWithTable("q", Question$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.SURVEY_ID_SURVEY))
                        .eq(this.getId()))
                .and(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.QUESTION_ID_PARENT)).isNull())
                .and(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.VALUE)).isNotNull())
                .and(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.VALUE)).isNot("")).queryList();
        //List<Value> values = Value.findWithQuery(Value.class, LIST_VALUES_PARENT_QUESTION, this.getId().toString());
        return values;
    }

    /**
     * Ratio of completion is cached into answeredQuestionRatio in order to speed up loading
     * @return
     */
    public SurveyAnsweredRatio getAnsweredQuestionRatio(){
        if (answeredQuestionRatio == null) {
            answeredQuestionRatio = SurveyAnsweredRatioCache.get(this.getId());
            if(answeredQuestionRatio == null) {
                answeredQuestionRatio = reloadSurveyAnsweredRatio();
            }
        }
        return answeredQuestionRatio;
    }

    /**
     * Calculates the current ratio of completion for this survey
     * @return SurveyAnsweredRatio that hold the total & answered questions.
     */
    private SurveyAnsweredRatio reloadSurveyAnsweredRatio(){
        int numRequired= Question.countRequiredByProgram(this.getProgram());
        int numOptional=0;
        int numAnswered = Value.countBySurvey(this);

        for (Value value : this.getValuesFromParentQuestions()) {
            if (value.isAYes()) {
                //There might be children no answer questions that should be skipped
                for(Question childQuestion:value.getQuestion().getChildren()){
                    numOptional+=(childQuestion.getAnswer().getOutput()==Constants.NO_ANSWER)?0:1;
                }
            }

        }
        SurveyAnsweredRatio surveyAnsweredRatio=new SurveyAnsweredRatio(numRequired+numOptional, numAnswered);
        SurveyAnsweredRatioCache.put(this.id, surveyAnsweredRatio);
        return surveyAnsweredRatio;
    }

    /**
     * Updates ratios, status and completion date depending on the question and answer (text)
     */
    public void updateSurveyStatus(){

        //Sent surveys are not updated
        if(this.isSent()){
            return;
        }

        SurveyAnsweredRatio answeredRatio=this.reloadSurveyAnsweredRatio();

        //Update status & completionDate
        if(answeredRatio.isCompleted()) {
            this.setStatus(Constants.SURVEY_COMPLETED);
            this.setCompletionDate(new Date());
        }else{
            this.setStatus(Constants.SURVEY_IN_PROGRESS);
            this.setCompletionDate(this.eventDate);
        }

        //Saves new status & completionDate
        this.save();
    }

    /**
     * Returns a concrete survey, if it exists
     * @param orgUnit
     * @param program
     * @return
     */
    public static List<Survey> getUnsentSurveys(OrgUnit orgUnit, Program program) {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.ORGUNIT_ID_ORG_UNIT).eq(orgUnit.getId()))
                .and(Condition.column(Survey$Table.PROGRAM_ID_PROGRAM).eq(program.getId()))
                .and(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_SENT))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ORGUNIT_ID_ORG_UNIT).queryList();
    }

    /**
     * Returns all the surveys with status yet not put to "Sent"
     * @return
     */
    public static List<Survey> getAllUnsentSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_SENT))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ORGUNIT_ID_ORG_UNIT).queryList();
    }

    /**
     * Returns the last surveys (by date) with status yet not put to "Sent"
     * @param limit
     * @return
     */
    public static List<Survey> getUnsentSurveys(int limit) {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_SENT))
                .limit(String.valueOf(limit))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ORGUNIT_ID_ORG_UNIT).queryList();
    }

    /**
     * Returns all the surveys with status put to "Sent"
     * @return
     */
    public static List<Survey> getAllSentSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_SENT))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ORGUNIT_ID_ORG_UNIT).queryList();
    }

    /**
     * Returns the last surveys (by date) with status put to "Sent"
     * @param limit
     * @return
     */
    public static List<Survey> getSentSurveys(int limit) {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_SENT))
                .limit(String.valueOf(limit))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ORGUNIT_ID_ORG_UNIT).queryList();
    }

    /**
     * Returns all the surveys with status put to "Completed"
     * @return
     */
    public static List<Survey> getAllCompletedSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_COMPLETED))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ORGUNIT_ID_ORG_UNIT).queryList();
    }

    /**
     * Returns the last surveys (by date) with status put to "Completed"
     * @param limit
     * @return
     */
    public static List<Survey> getCompletedSurveys(int limit) {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_COMPLETED))
                .limit(String.valueOf(limit))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ORGUNIT_ID_ORG_UNIT).queryList();
    }

    /**
     * Returns all the surveys with status put to "In progress"
     * @return
     */
    public static List<Survey> getAllUncompletedSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_IN_PROGRESS))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ORGUNIT_ID_ORG_UNIT).queryList();
    }

    /**
     * Returns the last surveys (by date) with status put to "In progress"
     * @param limit
     * @return
     */
    public static List<Survey> getUncompletedSurveys(int limit) {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_IN_PROGRESS))
                .limit(String.valueOf(limit))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ORGUNIT_ID_ORG_UNIT).queryList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Survey)) return false;

        Survey survey = (Survey) o;

        if (id != survey.id) return false;
        if (!program.equals(survey.program)) return false;
        if (!orgUnit.equals(survey.orgUnit)) return false;
        if (!user.equals(survey.user)) return false;
        if (eventDate != null ? !eventDate.equals(survey.eventDate) : survey.eventDate != null)
            return false;
        if (completionDate != null ? !completionDate.equals(survey.completionDate) : survey.completionDate != null)
            return false;
        return status.equals(survey.status);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + program.hashCode();
        result = 31 * result + orgUnit.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + (eventDate != null ? eventDate.hashCode() : 0);
        result = 31 * result + (completionDate != null ? completionDate.hashCode() : 0);
        result = 31 * result + status.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "id=" + id +
                ", program=" + program +
                ", orgUnit=" + orgUnit +
                ", user=" + user +
                ", eventDate=" + eventDate +
                ", completionDate=" + completionDate +
                ", status=" + status +
                '}';
    }
}
