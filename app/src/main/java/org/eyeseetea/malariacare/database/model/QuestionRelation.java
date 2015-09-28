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
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

import java.util.List;

/**
 * Created by Jose on 25/05/2015.
 */
@Table(databaseName = AppDatabase.NAME)
public class QuestionRelation extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id_question_relation;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_question",
            columnType = Long.class,
            foreignColumnName = "id_question")},
            saveForeignKeyModel = false)
    Question question;

    @Column
    int operation;

    List<Match> matches;

    /**
     * Constant that reflects a parent child relationship
     */
    public static final int PARENT_CHILD=1;
    /**
     * Constant that reflects a match relationship
     */
    public static final int MATCH=0;

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "matches")
    public List<Match> getMatches() {
        //if (this.children == null){
        this.matches = new Select().from(Match.class)
                .where(Condition.column(Match$Table.QUESTIONRELATION_ID_QUESTION_RELATION).eq(this.getId_question_relation()))
                .queryList();
        //}
        return this.matches;
    }



    public QuestionRelation(){};

    public QuestionRelation(Question question, int operation) {
        this.question = question;
        this.operation = operation;
    }

    public long getId_question_relation() {
        return id_question_relation;
    }

    public void setId_question_relation(long id_question_relation) {
        this.id_question_relation = id_question_relation;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuestionRelation that = (QuestionRelation) o;

        if (id_question_relation != that.id_question_relation) return false;
        if (operation != that.operation) return false;
        if (question != null ? !question.equals(that.question) : that.question != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id_question_relation ^ (id_question_relation >>> 32));
        result = 31 * result + (question != null ? question.hashCode() : 0);
        result = 31 * result + operation;
        return result;
    }

    @Override
    public String toString() {
        return "QuestionRelation{" +
                "id=" + id_question_relation +
                ", question=" + question +
                ", operation=" + operation +
                '}';
    }
}
