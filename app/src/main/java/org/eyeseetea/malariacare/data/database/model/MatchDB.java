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

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

import java.util.List;

/**
 * Created by Jose on 25/05/2015.
 */
@Table(database = AppDatabase.class, name = "Match")
public class MatchDB extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id_match;

    @Column
    Long id_question_relation_fk;
    /**
     * Reference to the associated questionRelation (loaded lazily)
     */
    QuestionRelationDB questionRelation;

    /**
     * List of questionOptions associated to this match
     */
    List<QuestionOptionDB> questionOptions;

    public MatchDB(){}

    public MatchDB(QuestionRelationDB questionRelation){
        setQuestionRelation(questionRelation);
    }

    public List<QuestionOptionDB> getQuestionOptions() {
        if(questionOptions==null){
            this.questionOptions = new Select().from(QuestionOptionDB.class)
                    .where(QuestionOptionDB_Table.id_match_fk.eq(this.getId_match()))
                    .queryList();
        }
        return this.questionOptions;
    }

    public long getId_match() {
        return id_match;
    }

    public void setId_match(long id_match) {
        this.id_match = id_match;
    }

    public QuestionRelationDB getQuestionRelation() {
        if(questionRelation==null){
            if(id_question_relation_fk==null) return null;
            questionRelation = new Select()
                    .from(QuestionRelationDB.class)
                    .where(QuestionRelationDB_Table.id_question_relation
                            .is(id_question_relation_fk)).querySingle();
        }
        return questionRelation;
    }

    public void setQuestionRelation(QuestionRelationDB questionRelation) {
        this.questionRelation = questionRelation;
        this.id_question_relation_fk = (questionRelation!=null)?questionRelation.getId_question_relation():null;
    }

    public void setQuestionRelation(Long id_question_relation){
        this.id_question_relation_fk = id_question_relation;
        this.questionRelation = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchDB match = (MatchDB) o;

        if (id_match != match.id_match) return false;
        return !(id_question_relation_fk != null ? !id_question_relation_fk.equals(match.id_question_relation_fk) : match.id_question_relation_fk != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_match ^ (id_match >>> 32));
        result = 31 * result + (id_question_relation_fk != null ? id_question_relation_fk.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Match{" +
                "id_match=" + id_match +
                ", id_question_relation=" + id_question_relation_fk +
                '}';
    }
}
