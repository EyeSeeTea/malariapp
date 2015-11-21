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
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;

import java.util.List;

/**
 * Created by Jose on 25/05/2015.
 */
@Table(databaseName = AppDatabase.NAME)
public class Match extends BaseModel implements VisitableToSDK {
    @Column
    @PrimaryKey(autoincrement = true)
    long id_match;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_question_relation",
            columnType = Long.class,
            foreignColumnName = "id_question_relation")},
            saveForeignKeyModel = false)
    QuestionRelation questionRelation;

    List<QuestionOption> questionOptions;

    public Match(){}

    public Match(QuestionRelation questionRelation){
        setQuestionRelation(questionRelation);
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "questionOptions")
    public List<QuestionOption> getQuestionOptions() {
        //if (this.children == null){
        this.questionOptions = new Select().from(QuestionOption.class)
                .where(Condition.column(QuestionOption$Table.MATCH_ID_MATCH).eq(this.getId_match()))
                .queryList();
        //}
        return this.questionOptions;
    }

    public long getId_match() {
        return id_match;
    }

    public void setId_match(long id_match) {
        this.id_match = id_match;
    }

    public QuestionRelation getQuestionRelation() {
        return questionRelation;
    }

    public void setQuestionRelation(QuestionRelation questionRelation) {
        this.questionRelation = questionRelation;
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) {
        IConvertToSDKVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Match match = (Match) o;

        if (id_match != match.id_match) return false;
        if (questionRelation != null ? !questionRelation.equals(match.questionRelation) : match.questionRelation != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id_match ^ (id_match >>> 32));
        result = 31 * result + (questionRelation != null ? questionRelation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QuestionRelation{" +
                "id=" + id_match +
                ", questionRelation=" + questionRelation +
                '}';
    }
}
