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

package org.eyeseetea.malariacare.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Answer extends BaseModel implements Visitable{

    @Column
    @PrimaryKey(autoincrement = true)
    long id_answer;
    @Column
    String name;
    @Column
    Integer output;

    List<Option> options;

    List<Question> questions;

    public Answer() {
    }

    public Answer(String name, Integer output) {
        this.name = name;
        this.output = output;
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

    public Integer getOutput() {
        return output;
    }

    public void setOutput(Integer output) {
        this.output = output;
    }

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "options")
    public List<Option> getOptions(){
        return new Select().from(Option.class).where(Condition.column(Option$Table.ANSWER_ID_ANSWER).eq(this.getId_answer())).queryList();
    }

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "questions")
    public List<Question> getQuestions(){
        return new Select().from(Question.class).where(Condition.column(Question$Table.ANSWER_ID_ANSWER).eq(this.getId_answer())).queryList();
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) {
        IConvertToSDKVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Answer)) return false;

        Answer answer = (Answer) o;

        if (id_answer != answer.id_answer) return false;
        if (name != null ? !name.equals(answer.name) : answer.name != null) return false;
        return output.equals(answer.output);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_answer ^ (id_answer >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + output.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id_answer +
                ", name='" + name + '\'' +
                ", output=" + output +
                '}';
    }
}
