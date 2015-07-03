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
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Survey extends SugarRecord<Survey> {

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
     * Ratio of completion is cached into _answeredQuestionRatio in order to speed up loading
     * @return
     */
    public SurveyAnsweredRatio getAnsweredQuestionRatio(){
        if (_answeredQuestionRatio == null) {
            _answeredQuestionRatio=reloadSurveyAnsweredRatio();
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

        for (Value value : this.getValues()) {
            if (value!=null && value.isAnAnswer()) {
                numAnswered++;
                if (value.belongsToAParentQuestion() && value.isAYes()) {
                    //There might be children no answer questions that should be skipped
                    for(Question childQuestion:value.getQuestion().getQuestionChildren()){
                        numOptional+=(childQuestion.getAnswer().getOutput()==Constants.NO_ANSWER)?0:1;
                    }
                }
            }
        }
        return new SurveyAnsweredRatio(numRequired+numOptional, numAnswered);
    }

    /**
     * Updates ratios, status and completion date depending on the question and answer (text)
     */
    public void updateSurveyStatus(){
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

    // Returns the 5 last surveys (by date) with status yet not put to "Sent"
    public static List<Survey> getUnsentSurveys(int limit) {
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").notEq(Constants.SURVEY_SENT))
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
