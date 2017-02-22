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
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

@Table(database = AppDatabase.class)
public class Option extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_option;
    @Column
    String uid;
    @Column
    String code;
    @Column
    String name;
    @Column
    Float factor;
    @Column
    Long id_answer;

    /**
     * Reference to parent answer (loaded lazily)
     */
    Answer answer;

    /**
     * List of values that has choosen this option
     */
    List<Value> values;

    public Option() {
    }

    public Option(String name, Float factor, Answer answer) {
        this.name = name;
        this.factor = factor;
        this.setAnswer(answer);
    }

    public Option(String code, String name, Float factor, Answer answer) {
        this.name = name;
        this.factor = factor;
        this.code = code;
        this.setAnswer(answer);
    }


    public Option(String name) {
        this.name = name;
    }

    public Long getId_option() {
        return id_option;
    }

    public void setId_option(Long id_option) {
        this.id_option = id_option;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getFactor() {
        return factor;
    }

    public void setFactor(Float factor) {
        this.factor = factor;
    }

    public Answer getAnswer() {
        if (answer == null) {
            if (id_answer == null) return null;
            answer = new Select()
                    .from(Answer.class)
                    .where(Answer_Table.id_answer
                            .is(id_answer)).querySingle();
        }
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
        this.id_answer = (answer != null) ? answer.getId_answer() : null;
    }

    public void setAnswer(Long id_answer) {
        this.id_answer = id_answer;
        this.answer = null;
    }

    /**
     * Checks if this option actives the children questions by a parentQuestion
     *
     * @return true: Children questions should be shown, false: otherwise.
     */
    public boolean isActiveChildren(Question question) {
        for (Match match : question.getMatches()) {
            if (isActiveChildren(match)) return true;
        }
        return false;
    }

    private boolean isActiveChildren(Match match) {
        for (QuestionOption questionOption : match.getQuestionOptions()) {
            if (questionOption.getOption().getId_option() == id_option) {
                QuestionRelation questionRelation = match.getQuestionRelation();
                if (questionRelation.getOperation() == Constants.OPERATION_TYPE_PARENT) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Checks if this option name is equals to a given string.
     *
     * @return true|false
     */
    public boolean is(String given) {
        return given.equals(name);
    }

    public List<Value> getValues() {
        if (values == null) {
            values = new Select().from(Value.class)
                    .where(Value_Table.id_option.eq(this.getId_option())).queryList();
        }
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        if (id_option != option.id_option) return false;
        if (uid != null ? !uid.equals(option.uid) : option.uid != null) return false;
        if (code != null ? !code.equals(option.code) : option.code != null) return false;
        if (name != null ? !name.equals(option.name) : option.name != null) return false;
        if (factor != null ? !factor.equals(option.factor) : option.factor != null) return false;
        if (id_answer != null ? !id_answer.equals(option.id_answer) : option.id_answer != null) {
            return false;
        }
        if (answer != null ? !answer.equals(option.answer) : option.answer != null) return false;
        return values != null ? values.equals(option.values) : option.values == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id_option ^ (id_option >>> 32));
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (factor != null ? factor.hashCode() : 0);
        result = 31 * result + (id_answer != null ? id_answer.hashCode() : 0);
        result = 31 * result + (answer != null ? answer.hashCode() : 0);
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Option{" +
                "id_option=" + id_option +
                ", uid='" + uid + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", factor=" + factor +
                ", id_answer=" + id_answer +
                ", answer=" + answer +
                ", values=" + values +
                '}';
    }
}
