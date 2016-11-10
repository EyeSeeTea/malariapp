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

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

import java.util.Date;

/**
 * Created by ivan.arrizabalaga on 14/02/15.
 */
@Table(database = AppDatabase.class)
public class SurveySchedule extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_survey_schedule;

    @Column
    Long id_survey;
    /**
     * Reference to the survey associated to this score (loaded lazily)
     */
    Survey survey;

    @Column
    String comment;

    @Column
    Date previous_date;

    public SurveySchedule() {
    }

    public SurveySchedule(Survey survey, Date previous_date, String comment) {
        this.previous_date = previous_date;
        this.comment = comment;
        this.setSurvey(survey);
    }

    public long getId_survey_schedule() {
        return id_survey_schedule;
    }

    public void setId_survey_schedule(long id_survey_schedule) {
        this.id_survey_schedule = id_survey_schedule;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getPrevious_date() {
        return previous_date;
    }

    public void setPrevious_date(Date previous_date) {
        this.previous_date = previous_date;
    }

    public Survey getSurvey() {
        if(survey==null){
            if(id_survey==null) return null;
            survey = new Select()
                    .from(Survey.class)
                    .where(Condition.column(Survey$Table.ID_SURVEY)
                            .is(id_survey)).querySingle();
        }
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
        this.id_survey = (survey!=null)?survey.getId_survey():null;
    }

    public void setSurvey(Long id_survey){
        this.id_survey = id_survey;
        this.survey = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurveySchedule that = (SurveySchedule) o;

        if (id_survey_schedule != that.id_survey_schedule) return false;
        if (id_survey != null ? !id_survey.equals(that.id_survey) : that.id_survey != null)
            return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        return !(previous_date != null ? !previous_date.equals(that.previous_date) : that.previous_date != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_survey_schedule ^ (id_survey_schedule >>> 32));
        result = 31 * result + (id_survey != null ? id_survey.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (previous_date != null ? previous_date.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SurveySchedule{" +
                "id_survey_schedule=" + id_survey_schedule +
                ", id_survey=" + id_survey +
                ", comment='" + comment + '\'' +
                ", previous_date=" + previous_date +
                '}';
    }
}
