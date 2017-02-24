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

/**
 * Created by Jose on 25/05/2015.
 */
@Table(database = AppDatabase.class)
public class QuestionOption extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id_question_option;

    @Column
    Long id_option_fk;
    /**
     * Reference to its option (lazy)
     */
    Option option;

    @Column
    Long id_question_fk;
    /**
     * Reference to its question (lazy)
     */
    Question question;

    @Column
    Long id_match_fk;
    /**
     * Reference to its match (lazy)
     */
    Match match;

    public QuestionOption(){}

    public QuestionOption(Option option, Question question, Match match) {
        setQuestion(question);
        setOption(option);
        setMatch(match);
    }

    public long getId_question_option() {
        return id_question_option;
    }

    public void setId_question_option(long id_question_option) {
        this.id_question_option = id_question_option;
    }

    public Option getOption() {
        if(option==null){
            if(id_option_fk==null) return null;
            option = new Select()
                    .from(Option.class)
                    .where(Option_Table.id_option
                            .is(id_option_fk)).querySingle();
        }
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
        this.id_option_fk = (option!=null)?option.getId_option():null;
    }

    public void setOption(Long id_option){
        this.id_option_fk = id_option;
        this.option = null;
    }

    public Question getQuestion() {
        if(question==null){
            if(id_question_fk==null) return null;
            question = new Select()
                    .from(Question.class)
                    .where(Question_Table.id_question
                            .is(id_question_fk)).querySingle();
        }
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
        this.id_question_fk = (question!=null)?question.getId_question():null;
    }

    public void setQuestion(Long id_question){
        this.id_question_fk = id_question;
        this.question = null;
    }

    public Match getMatch() {
        if(match==null){
            if(id_match_fk==null) return null;
            match = new Select()
                    .from(Match.class)
                    .where(Match_Table.id_match
                            .is(id_match_fk)).querySingle();
        }
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
        this.id_match_fk = (match!=null)?match.getId_match():null;
    }

    public void setMatch(Long id_match){
        this.id_match_fk = id_match;
        this.match = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuestionOption that = (QuestionOption) o;

        if (id_question_option != that.id_question_option) return false;
        if (id_option_fk != null ? !id_option_fk.equals(that.id_option_fk) : that.id_option_fk != null)
            return false;
        if (id_question_fk != null ? !id_question_fk.equals(that.id_question_fk) : that.id_question_fk != null)
            return false;
        return !(id_match_fk != null ? !id_match_fk.equals(that.id_match_fk) : that.id_match_fk != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_question_option ^ (id_question_option >>> 32));
        result = 31 * result + (id_option_fk != null ? id_option_fk.hashCode() : 0);
        result = 31 * result + (id_question_fk != null ? id_question_fk.hashCode() : 0);
        result = 31 * result + (id_match_fk != null ? id_match_fk.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QuestionOption{" +
                "id_question_option=" + id_question_option +
                ", id_option=" + id_option_fk +
                ", id_question=" + id_question_fk +
                ", id_match=" + id_match_fk +
                '}';
    }
}
