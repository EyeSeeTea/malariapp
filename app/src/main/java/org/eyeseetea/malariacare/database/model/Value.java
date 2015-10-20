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
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

@Table(databaseName = AppDatabase.NAME)
public class Value extends BaseModel implements Visitable {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_value;
    @Column
    String value;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_question",
            columnType = Long.class,
            foreignColumnName = "id_question")},
            saveForeignKeyModel = false)
    Question question;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_survey",
            columnType = Long.class,
            foreignColumnName = "id_survey")},
            saveForeignKeyModel = false)
    Survey survey;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_option",
            columnType = Long.class,
            foreignColumnName = "id_option")},
            saveForeignKeyModel = false)
    Option option;

    public Value() {
    }

    public Value(String value, Question question, Survey survey) {
        this.option = null;
        this.question = question;
        this.value = value;
        this.survey = survey;
    }

    public Value(Option option, Question question, Survey survey) {
        this.option = option;
        this.question = question;
        this.value = option.getName();
        this.survey = survey;
    }

    public Long getId_value() {
        return id_value;
    }

    public void setId_value(Long id_value) {
        this.id_value = id_value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    /**
     * Checks if the current value contains an answer
     * @return true|false
     */
    public boolean isAnAnswer(){
        return (getValue() != null && !getValue().equals("")) || getOption() != null;
    }

    /**
     * Checks if the current value belongs to a 'required' question
     * @return
     */
    public boolean belongsToAParentQuestion(){
        return !getQuestion().hasParent();
    }

    /**
     * The value is 'Yes' from a dropdown
     * @return true|false
     */
    public boolean isAYes() {
        return getOption() != null && getOption().getName().equals("Yes");
    }

    public static int countBySurvey(Survey survey){
        if(survey==null || survey.getId_survey()==null){
            return 0;
        }
        return (int) new Select().count()
                .from(Value.class)
                .where(Condition.column(Value$Table.SURVEY_ID_SURVEY).eq(survey.getId_survey())).count();
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) {
        IConvertToSDKVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Value)) return false;

        Value value1 = (Value) o;

        if (id_value != value1.id_value) return false;
        if (!value.equals(value1.value)) return false;
        if (!question.equals(value1.question)) return false;
        if (!survey.equals(value1.survey)) return false;
        return !(option != null ? !option.equals(value1.option) : value1.option != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_value ^ (id_value >>> 32));
        result = 31 * result + value.hashCode();
        result = 31 * result + question.hashCode();
        result = 31 * result + survey.hashCode();
        result = 31 * result + (option != null ? option.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Value{" +
                "id=" + id_value +
                ", value='" + value + '\'' +
                ", question=" + question +
                ", survey=" + survey +
                ", option=" + option +
                '}';
    }
}
