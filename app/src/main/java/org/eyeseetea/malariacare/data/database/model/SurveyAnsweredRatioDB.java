/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
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

package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

@Table(database = AppDatabase.class, name = "SurveyAnsweredRatio")
public class SurveyAnsweredRatioDB extends BaseModel{

    @Column
    @PrimaryKey(autoincrement = true)
    long id_survey_answered_ratio;
    /**
     * Id of the survey
     */
    @Column
    long id_survey;
    /**
     * Total number of questions to answer
     */
    @Column
    int total_questions;

    /**
     * Total number of answered questions
     */
    @Column
    int answered_questions;

    /**
     * Total number of compulsoryAnswered questions
     */
    @Column
    int total_compulsory_questions;
    /**
     * Total number of compulsoryAnswered questions
     */
    @Column
    int answered_compulsory_questions;

    public SurveyAnsweredRatioDB() {
    }

    public static void saveEntityToModel(SurveyAnsweredRatio surveyAnsweredRatio){
        SurveyAnsweredRatioDB surveyAnsweredRatioDB = getSurveyAnsweredRatioBySurveyId(
                surveyAnsweredRatio.getSurveyId());
        if(surveyAnsweredRatioDB ==null){
            surveyAnsweredRatioDB = new SurveyAnsweredRatioDB();
        }
        surveyAnsweredRatioDB.id_survey = surveyAnsweredRatio.getSurveyId();
        surveyAnsweredRatioDB.total_questions = surveyAnsweredRatio.getTotal();
        surveyAnsweredRatioDB.answered_questions = surveyAnsweredRatio.getAnswered();
        surveyAnsweredRatioDB.total_compulsory_questions = surveyAnsweredRatio.getTotalCompulsory();
        surveyAnsweredRatioDB.answered_compulsory_questions = surveyAnsweredRatio.getCompulsoryAnswered();
        surveyAnsweredRatioDB.save();
    }
    public static SurveyAnsweredRatioDB getSurveyAnsweredRatioBySurveyId(long id_survey){
        return new Select().from(SurveyAnsweredRatioDB.class)
                .where(SurveyAnsweredRatioDB_Table.id_survey.is(id_survey)).querySingle();
    }


    public long getIdSurveyAnsweredRatio() {
        return id_survey_answered_ratio;
    }

    public void setIdSurveyAnsweredRatio(long id_survey_answered_ratio) {
        this.id_survey_answered_ratio = id_survey_answered_ratio;
    }

    public int getTotalQuestions() {
        return total_questions;
    }

    public void setTotalQuestions(int total_questions) {
        this.total_questions = total_questions;
    }

    public int getAnsweredQuestions() {
        return answered_questions;
    }

    public void setAnsweredQuestions(int answered_questions) {
        this.answered_questions = answered_questions;
    }

    public int getTotalCompulsoryQuestions() {
        return total_compulsory_questions;
    }

    public void setTotalCompulsoryQuestions(int total_compulsory_questions) {
        this.total_compulsory_questions = total_compulsory_questions;
    }

    public int getAnsweredCompulsoryQuestions() {
        return answered_compulsory_questions;
    }

    public void setAnsweredCompulsoryQuestions(int answered_compulsory_questions) {
        this.answered_compulsory_questions = answered_compulsory_questions;
    }

    public long getIdSurvey() {
        return id_survey;
    }

    public void setIdSurvey(long id_survey) {
        this.id_survey = id_survey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurveyAnsweredRatioDB that = (SurveyAnsweredRatioDB) o;

        if (id_survey_answered_ratio != that.id_survey_answered_ratio) return false;
        if (id_survey != that.id_survey) return false;
        if (total_questions != that.total_questions) return false;
        if (answered_questions != that.answered_questions) return false;
        if (total_compulsory_questions != that.total_compulsory_questions) return false;
        return answered_compulsory_questions == that.answered_compulsory_questions;

    }

    @Override
    public int hashCode() {
        int result = (int) (id_survey_answered_ratio ^ (id_survey_answered_ratio >>> 32));
        result = 31 * result + (int) (id_survey ^ (id_survey >>> 32));
        result = 31 * result + total_questions;
        result = 31 * result + answered_questions;
        result = 31 * result + total_compulsory_questions;
        result = 31 * result + answered_compulsory_questions;
        return result;
    }
}