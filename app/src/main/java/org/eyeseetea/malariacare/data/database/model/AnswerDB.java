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

import java.util.List;

@Table(database = AppDatabase.class, name = "Answer")
public class AnswerDB extends BaseModel{

    /**
     * Default mock answer.output value
     */
    public static final Integer DEFAULT_ANSWER_OUTPUT = -1;

    @Column
    @PrimaryKey(autoincrement = true)
    long id_answer;
    @Column
    String name;

    /**
     * List of options that belongs to this answer type
     */
    List<OptionDB> options;

    /**
     * List of options that have this answer type
     */
    List<QuestionDB> questions;

    public AnswerDB() {
    }

    public AnswerDB(String name) {
        this.name = name;
    }

    public Long getId_answer() {
        return id_answer;
    }

    public void setId_answer(Long id_answer) {
        this.id_answer = id_answer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<OptionDB> getOptions(){
        if(options==null){
            options = new Select()
                    .from(OptionDB.class)
                    .where(OptionDB_Table.id_answer_fk
                            .eq(this.getId_answer())).queryList();
        }
        return options;
    }

    public List<QuestionDB> getQuestions(){
        if(questions==null){
            questions = new Select()
                    .from(QuestionDB.class)
                    .where(QuestionDB_Table.id_answer_fk
                            .eq(this.getId_answer())).queryList();
        }
        return questions;
    }

    public static AnswerDB getByName(String answerName) {
        AnswerDB answerDB = new Select()
                .from(AnswerDB.class)
                .where(AnswerDB_Table.name
                        .eq(answerName)).querySingle();
        return answerDB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnswerDB answer = (AnswerDB) o;

        if (id_answer != answer.id_answer) return false;
        return !(name != null ? !name.equals(answer.name) : answer.name != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_answer ^ (id_answer >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id_answer=" + id_answer +
                ", name='" + name + '\'' +
                '}';
    }
}
