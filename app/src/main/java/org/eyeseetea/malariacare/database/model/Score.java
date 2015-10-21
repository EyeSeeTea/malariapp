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
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

/**
 * Created by adrian on 14/02/15.
 */
@Table(databaseName = AppDatabase.NAME)
public class Score extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_score;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_survey",
            columnType = Long.class,
            foreignColumnName = "id_survey")},
            saveForeignKeyModel = false)
    Survey survey;

    @Column
    String uid;

    @Column
    Float score;

    public Score() {
    }

    public Score(Survey survey, String uid, Float score) {
        this.survey = survey;
        this.uid = uid;
        this.score = score;
    }

    public Long getId_score() {
        return id_score;
    }

    public void setId_score(Long id_score) {
        this.id_score = id_score;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
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
}
