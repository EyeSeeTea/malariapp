/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.database.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.query.Condition;
import com.orm.query.Select;

import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatioCache;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.Date;
import java.util.List;

public class Survey extends SugarRecord<Survey> {


    private static final String LIST_VALUES_PARENT_QUESTION ="select v.* from value v"+
            " left join question q on v.question=q.id"+
            " where v.survey=?"+
            " and q.question=0"+
            " and v.value is not null and v.value<>''";

    OrgUnit orgUnit;
    Program program;
    User user;
    Date eventDate;
    Date completionDate;
    Integer status;

    @Ignore
    SurveyAnsweredRatio _answeredQuestionRatio;

    public Survey() {
    }

    public Survey(OrgUnit orgUnit, Program program, User user) {
        this.orgUnit = orgUnit;
        this.program = program;
        this.user = user;
        this.eventDate = new Date();
        this.status = Constants.SURVEY_IN_PROGRESS; // Possibilities [ In progress | Completed | Sent ]
        this.completionDate= this.eventDate;

        Log.i(".Survey", Long.valueOf(this.completionDate.getTime()).toString());
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

    public List<Value> getValues(){
        return Select.from(Value.class)
                .where(Condition.prop("survey")
                        .eq(String.valueOf(this.getId()))).list();
    }

    /**
     * Returns the list of answered values from this survey that belong to a parent question
     * @return
     */
    public List<Value> getValuesFromParentQuestions(){
        List<Value> values = Value.findWithQuery(Value.class, LIST_VALUES_PARENT_QUESTION, this.getId().toString());
        return values;
    }

    /**
     * Ratio of completion is cached into _answeredQuestionRatio in order to speed up loading
     * @return
     */
    public SurveyAnsweredRatio getAnsweredQuestionRatio(){
        if (_answeredQuestionRatio == null) {
            _answeredQuestionRatio=SurveyAnsweredRatioCache.get(this.id);
            if(_answeredQuestionRatio == null) {
                _answeredQuestionRatio = reloadSurveyAnsweredRatio();
            }
        }
        return _answeredQuestionRatio;
    }

    /**
     * Calculates the current ratio of completion for this survey
     * @return SurveyAnsweredRatio that hold the total & answered questions.
     */
    private SurveyAnsweredRatio reloadSurveyAnsweredRatio(){

        int numRequired= Question.countRequiredByProgram(this.getProgram());
        int numOptional=0;
        int numAnswered = 0;

        for (Value value : this.getValuesFromParentQuestions()) {
            numAnswered++;
            if (value.isAYes()) {
                //There might be children no answer questions that should be skipped
                for(Question childQuestion:value.getQuestion().getQuestionChildren()){
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

    // Returns a concrete survey, if it exists
    public static List<Survey> getUnsentSurveys(OrgUnit orgUnit, Program program) {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("org_unit").eq(orgUnit.getId()))
                .and(com.orm.query.Condition.prop("program").eq(program.getId()))
                .and(com.orm.query.Condition.prop("status").notEq(Constants.SURVEY_SENT))
                .orderBy("event_date")
                .orderBy("org_unit")
                .list();
    }

    // Returns all the surveys with status yet not put to "Sent"
    public static List<Survey> getAllUnsentSurveys() {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").notEq(Constants.SURVEY_SENT))
                .orderBy("event_date")
                .orderBy("org_unit")
                .list();
    }

    // Returns the last surveys (by date) with status yet not put to "Sent"
    public static List<Survey> getUnsentSurveys(int limit) {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").notEq(Constants.SURVEY_SENT))
                .limit(String.valueOf(limit))
                .orderBy("event_date")
                .orderBy("org_unit")
                .list();
    }

    // Returns all the surveys with status put to "Sent"
    public static List<Survey> getAllSentSurveys() {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").eq(Constants.SURVEY_SENT))
                .orderBy("event_date")
                .orderBy("org_unit")
                .list();
    }

    // Returns the last surveys (by date) with status put to "Sent"
    public static List<Survey> getSentSurveys(int limit) {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").eq(Constants.SURVEY_SENT))
                .limit(String.valueOf(limit))
                .orderBy("event_date")
                .orderBy("org_unit")
                .list();
    }

    // Returns all the surveys with status put to "Completed"
    public static List<Survey> getAllCompletedSurveys() {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").eq(Constants.SURVEY_COMPLETED))
                .orderBy("event_date")
                .orderBy("org_unit")
                .list();
    }

    // Returns the last surveys (by date) with status put to "Completed"
    public static List<Survey> getCompletedSurveys(int limit) {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").eq(Constants.SURVEY_COMPLETED))
                .limit(String.valueOf(limit))
                .orderBy("event_date")
                .orderBy("org_unit")
                .list();
    }

    // Returns all the surveys with status put to "In progress"
    public static List<Survey> getAllUncompletedSurveys() {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").eq(Constants.SURVEY_IN_PROGRESS))
                .orderBy("event_date")
                .orderBy("org_unit")
                .list();
    }

    // Returns the last surveys (by date) with status put to "In progress"
    public static List<Survey> getUncompletedSurveys(int limit) {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").eq(Constants.SURVEY_IN_PROGRESS))
                .limit(String.valueOf(limit))
                .orderBy("event_date")
                .orderBy("org_unit")
                .list();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Survey)) return false;

        Survey survey = (Survey) o;

        if (_answeredQuestionRatio != null ? !_answeredQuestionRatio.equals(survey._answeredQuestionRatio) : survey._answeredQuestionRatio != null)
            return false;
        if (!eventDate.equals(survey.eventDate)) return false;
        if (!completionDate.equals(survey.completionDate)) return false;
        if (!orgUnit.equals(survey.orgUnit)) return false;
        if (!program.equals(survey.program)) return false;
        if (!status.equals(survey.status)) return false;
        if (!user.equals(survey.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = orgUnit.hashCode();
        result = 31 * result + program.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + eventDate.hashCode();
        result = 31 * result + completionDate.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + (_answeredQuestionRatio != null ? _answeredQuestionRatio.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "orgUnit=" + orgUnit +
                ", program=" + program +
                ", user=" + user +
                ", eventDate=" + eventDate +
                ", completionDate=" + completionDate +
                ", status=" + status +
                '}';
    }
}
