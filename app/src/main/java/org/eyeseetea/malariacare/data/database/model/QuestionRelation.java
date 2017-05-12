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

import android.util.Log;

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
@Table(database = AppDatabase.class)

public class QuestionRelation extends BaseModel {

    private static final String TAG = ".QuestionRelation";
    /**
     * Constant that reflects a parent child relationship
     */
    public static final int PARENT_CHILD=1;
    /**
     * Constant that reflects a match relationship
     */
    public static final int MATCH=0;

    @Column
    @PrimaryKey(autoincrement = true)
    long id_question_relation;
    @Column
    Long id_question_fk;
    /**
     * Reference to associated question (loaded lazily)
     */
    Question question;

    @Column
    int operation;

    /**
     * List of matches associated to this questionRelation
     */
    List<Match> matches;

    public QuestionRelation(){}

    public QuestionRelation(Question question, int operation) {
        this.operation = operation;
        this.setQuestion(question);
    }

    public long getId_question_relation() {
        return id_question_relation;
    }

    public void setId_question_relation(long id_question_relation) {
        this.id_question_relation = id_question_relation;
    }

    public Question getQuestion() {
        if(question==null){
            if(id_question_fk==null) return null;
            question = new Select()
                    .from(Question.class)
                    .where( Question_Table.id_question
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

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public void createMatchFromQuestions(List<Question> children){
        if (children.size() != 2){
            Log.e(TAG, "createMatchFromQuestions(): children must be 2. Match not created");
            return;
        }
        Match match;
        for (Option optionA : children.get(0).getAnswer().getOptions()) {
            for (Option optionB : children.get(1).getAnswer().getOptions()) {
                if(optionA.getFactor().equals(optionB.getFactor())){
                    //Save all optiona factor optionb factor with the same match
                    match = new Match(this);
                    match.save();
                    new QuestionOption(optionA, children.get(0), match).save();
                    new QuestionOption(optionB, children.get(1), match).save();
                }
            }
        }
    }

    public List<Match> getMatches() {
        if(matches==null) {
            this.matches = new Select().from(Match.class)
                    //// FIXME: 11/11/2016
                    //.indexedBy("Match_id_question_relation")
                    .where( Match_Table.id_question_relation_fk.eq(this.getId_question_relation()))
                    .queryList();
        }
        return this.matches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuestionRelation that = (QuestionRelation) o;

        if (id_question_relation != that.id_question_relation) return false;
        if (operation != that.operation) return false;
        return !(id_question_fk != null ? !id_question_fk.equals(that.id_question_fk) : that.id_question_fk != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_question_relation ^ (id_question_relation >>> 32));
        result = 31 * result + (id_question_fk != null ? id_question_fk.hashCode() : 0);
        result = 31 * result + operation;
        return result;
    }

    @Override
    public String toString() {
        return "QuestionRelation{" +
                "id_question_relation=" + id_question_relation +
                ", id_question=" + id_question_fk +
                ", operation=" + operation +
                '}';
    }
}
