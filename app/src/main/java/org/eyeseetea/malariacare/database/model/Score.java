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
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;

/**
 * Created by adrian on 14/02/15.
 */
@Table(database = AppDatabase.class)
public class Score extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_score;

    @Column
    Long id_survey;
    /**
     * Reference to the survey associated to this score (loaded lazily)
     */
    Survey survey;

    @Column
    String uid;

    @Column
    Float score;

    public Score() {
    }

    public Score(Survey survey, String uid, Float score) {
        this.uid = uid;
        this.score = score;
        this.setSurvey(survey);
    }

    public Long getId_score() {
        return id_score;
    }

    public void setId_score(Long id_score) {
        this.id_score = id_score;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Score score1 = (Score) o;

        if (id_score != score1.id_score) return false;
        if (id_survey != null ? !id_survey.equals(score1.id_survey) : score1.id_survey != null)
            return false;
        if (uid != null ? !uid.equals(score1.uid) : score1.uid != null) return false;
        return !(score != null ? !score.equals(score1.score) : score1.score != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_score ^ (id_score >>> 32));
        result = 31 * result + (id_survey != null ? id_survey.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id_score=" + id_score +
                ", id_survey=" + id_survey +
                ", uid='" + uid + '\'' +
                ", score=" + score +
                '}';
    }
}
