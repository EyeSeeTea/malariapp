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
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;

/**
 * Created by Jose on 25/05/2015.
 */
@Table(databaseName = AppDatabase.NAME)
public class QuestionOption extends BaseModel implements VisitableToSDK {
    @Column
    @PrimaryKey(autoincrement = true)
    long id_question_option;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_option",
            columnType = Long.class,
            foreignColumnName = "id_option")},
            saveForeignKeyModel = false)
    Option option;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_question",
            columnType = Long.class,
            foreignColumnName = "id_question")},
            saveForeignKeyModel = false)
    Question question;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_match",
            columnType = Long.class,
            foreignColumnName = "id_match")},
            saveForeignKeyModel = false)
    Match match;


    public QuestionOption(){

    }

    public QuestionOption(Option option, Question question, Match match) {
        this.option = option;
        this.question = question;
        this.match = match;
    }

    public long getId_question_option() {
        return id_question_option;
    }

    public void setId_question_option(long id_question_option) {
        this.id_question_option = id_question_option;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) {
        IConvertToSDKVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuestionOption that = (QuestionOption) o;

        if (id_question_option != that.id_question_option) return false;
        if (match != null ? !match.equals(that.match) : that.match != null) return false;
        if (option != null ? !option.equals(that.option) : that.option != null) return false;
        if (question != null ? !question.equals(that.question) : that.question != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id_question_option ^ (id_question_option >>> 32));
        result = 31 * result + (option != null ? option.hashCode() : 0);
        result = 31 * result + (question != null ? question.hashCode() : 0);
        result = 31 * result + (match != null ? match.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QuestionRelation{" +
                "id=" + id_question_option +
                ", option=" + option +
                ", question=" + question +
                ", match=" + match +
                '}';
    }
}
