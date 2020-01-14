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

@Table(database = AppDatabase.class, name = "Option")
public class OptionDB extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_option;
    @Column
    String uid_option;
    @Column
    String code;
    @Column
    String name;
    @Column
    Float factor;
    @Column
    Long id_answer_fk;

    /**
     * Reference to parent answer (loaded lazily)
     */
    AnswerDB answer;

    /**
     * List of values that has choosen this option
     */
    List<ValueDB> values;

    public OptionDB() {
    }

    public OptionDB(String name, Float factor, AnswerDB answer) {
        this.name = name;
        this.factor = factor;
        this.setAnswer(answer);
    }

    public OptionDB(String code, String name, Float factor, AnswerDB answer) {
        this.name = name;
        this.factor = factor;
        this.code = code;
        this.setAnswer(answer);
    }


    public OptionDB(String name) {
        this.name = name;
    }

    public static List<OptionDB> list() {
        return new Select().from(OptionDB.class).queryList();
    }

    public Long getId_option() {
        return id_option;
    }

    public void setId_option(Long id_option) {
        this.id_option = id_option;
    }

    public String getUid() {
        return uid_option;
    }

    public void setUid(String uid) {
        this.uid_option = uid;
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

    public AnswerDB getAnswer() {
        if(answer==null){
            if(id_answer_fk==null) return null;
            answer = new Select()
                    .from(AnswerDB.class)
                    .where(AnswerDB_Table.id_answer
                            .is(id_answer_fk)).querySingle();
        }
        return answer;
    }

    public void setAnswer(AnswerDB answer) {
        this.answer = answer;
        this.id_answer_fk = (answer!=null)?answer.getId_answer():null;
    }

    public void setAnswer(Long id_answer){
        this.id_answer_fk = id_answer;
        this.answer = null;
    }

    /**
     * Checks if this option actives the children questions by a parentQuestion
     *
     * @return true: Children questions should be shown, false: otherwise.
     */
    public boolean isActiveChildren(QuestionDB question) {
        for (MatchDB match : question.getMatches()) {
            if (isActiveChildren(match)) return true;
        }
        return false;
    }

    private boolean isActiveChildren(MatchDB match) {
        for (QuestionOptionDB questionOption : match.getQuestionOptions()) {
            if (questionOption.getOption().getId_option() == id_option) {
                QuestionRelationDB questionRelation = match.getQuestionRelation();
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

    public List<ValueDB> getValues() {
        if (values == null) {
            values = new Select().from(ValueDB.class)
                    .where(ValueDB_Table.id_option_fk.eq(this.getId_option())).queryList();
        }
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OptionDB option = (OptionDB) o;

        if (id_option != option.id_option) return false;
        if (uid_option != null ? !uid_option.equals(option.uid_option) : option.uid_option != null) return false;
        if (code != null ? !code.equals(option.code) : option.code != null) return false;
        if (name != null ? !name.equals(option.name) : option.name != null) return false;
        if (factor != null ? !factor.equals(option.factor) : option.factor != null) return false;
        if (id_answer_fk != null ? !id_answer_fk.equals(option.id_answer_fk) : option.id_answer_fk != null)
            return false;
        if (answer != null ? !answer.equals(option.answer) : option.answer != null) return false;
        return values != null ? values.equals(option.values) : option.values == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id_option ^ (id_option >>> 32));
        result = 31 * result + (uid_option != null ? uid_option.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (factor != null ? factor.hashCode() : 0);
        result = 31 * result + (id_answer_fk != null ? id_answer_fk.hashCode() : 0);
        result = 31 * result + (answer != null ? answer.hashCode() : 0);
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Option{" +
                "id_option=" + id_option +
                ", uid_option='" + uid_option + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", factor=" + factor +
                ", id_answer_fk=" + id_answer_fk +
                ", answer=" + answer +
                ", values=" + values +
                '}';
    }
}
