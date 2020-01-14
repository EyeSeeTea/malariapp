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

import static org.eyeseetea.malariacare.data.database.AppDatabase.questionAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.valueAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.valueName;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.VisitableToSDK;

import java.util.Date;

@Table(database = AppDatabase.class, name = "Value")
public class ValueDB extends BaseModel implements VisitableToSDK {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_value;
    @Column
    String value;

    @Column
    Long id_question_fk;
    /**
     * Reference to the question for this value (loaded lazily)
     */

    QuestionDB question;

    @Column
    Long id_survey_fk;
    /**
     * Reference to the survey of this value (loaded lazily)
     */
    SurveyDB survey;

    @Column
    Long id_option_fk;
    /**
     * Reference to the option of this value (loaded lazily)
     */
    OptionDB option;
    /**
     * is conflict
     */
    @Column
    Boolean conflict;

    @Column
    Date upload_date;

    public ValueDB() {
        upload_date =new Date();
    }

    public ValueDB(String value, QuestionDB question, SurveyDB survey) {
        this.option = null;
        this.value = value;
        this.setQuestion(question);
        this.setSurvey(survey);
        upload_date =new Date();
    }

    public ValueDB(OptionDB option, QuestionDB question, SurveyDB survey) {
        this.value = (option!=null)?option.getName():null;
        this.setOption(option);
        this.setQuestion(question);
        this.setSurvey(survey);
        upload_date =new Date();
    }

    public Long getId_value() {
        return id_value;
    }

    public void setId_value(Long id_value) {
        this.id_value = id_value;
    }


    public Long getId_survey_fk() {
        return id_survey_fk;
    }

    public Long getId_option_fk() {
        return id_option_fk;
    }

    public OptionDB getOption() {
        if(option==null){
            if(id_option_fk==null) return null;
            option = new Select()
                    .from(OptionDB.class)
                    .where(OptionDB_Table.id_option
                            .is(id_option_fk)).querySingle();
        }
        return option;
    }

    public void setOption(OptionDB option) {
        this.option = option;
        this.id_option_fk=(option!=null)?option.getId_option():null;
    }

    public void setOption(Long id_option){
        this.id_option_fk=id_option;
        this.option=null;
    }

    public Long getId_question_fk() {
        return id_question_fk;
    }

    public QuestionDB getQuestion() {
        if(question==null){
            if(id_question_fk==null) return null;
            question = new Select()
                    .from(QuestionDB.class)
                    .where(QuestionDB_Table.id_question
                            .is(id_question_fk)).querySingle();
        }

        return question;
    }

    public void setQuestion(QuestionDB question) {
        this.question = question;
        this.id_question_fk = (question!=null)?question.getId_question():null;
    }

    public void setQuestion(Long id_question){
        this.id_question_fk = id_question;
        this.question = null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SurveyDB getSurvey() {
        if(survey==null){
            if(id_survey_fk==null) return null;
            survey = new Select()
                    .from(SurveyDB.class)
                    .where(SurveyDB_Table.id_survey
                            .is(id_survey_fk)).querySingle();
        }
        return survey;
    }

    public void setSurvey(SurveyDB survey) {
        this.survey = survey;
        this.id_survey_fk = (survey!=null)?survey.getId_survey():null;
    }

    public void setSurvey(Long id_survey){
        this.id_survey_fk = id_survey;
        this.survey = null;
    }

    public Boolean getConflict() {
        if(conflict==null)
            return false;
        return conflict;
    }

    public void setConflict(Boolean conflict) {
        this.conflict = conflict;
    }

    public Date getUploadDate() {
        return upload_date;
    }

    public void setUploadDate(Date upload_date) {
        this.upload_date = upload_date;
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
     * The value is 'Positive' from a dropdown
     * @return true|false
     */
    public boolean isAPositive() {
        return getOption() != null && getOption().getName().equals("Positive");
    }

    /**
     * The value is 'Yes' from a dropdown
     * @return true|false
     */
    public boolean isAYes() {
        return getOption() != null && getOption().getName().equals("Yes");
    }

    public static int countBySurvey(SurveyDB survey){
        if(survey==null || survey.getId_survey()==null){
            return 0;
        }
        return (int) SQLite.selectCountOf()
                .from(ValueDB.class)
                .where(ValueDB_Table.id_survey_fk.eq(survey.getId_survey())).count();
    }

    public static int countCompulsoryBySurvey(SurveyDB survey){
        if(survey==null || survey.getId_survey()==null){
            return 0;
        }
        return (int) SQLite.selectCountOf()
                .from(ValueDB.class).as(valueName)
                .join(QuestionDB.class, Join.JoinType.LEFT_OUTER).as(questionName)
                .on(ValueDB_Table.id_question_fk.withTable(valueAlias).eq(QuestionDB_Table.id_question.withTable(questionAlias)))
                .where(QuestionDB_Table.compulsory.withTable(questionAlias).eq(true))
                .and(ValueDB_Table.id_survey_fk.withTable(valueAlias).eq(survey.getId_survey())).count();
    }

    public static long count(){
        return SQLite.selectCountOf()
                .from(ValueDB.class)
                .count();
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) {
        IConvertToSDKVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueDB value1 = (ValueDB) o;

        if (id_value != value1.id_value) return false;
        if (value != null ? !value.equals(value1.value) : value1.value != null) return false;
        if (id_question_fk != null ? !id_question_fk.equals(value1.id_question_fk) : value1.id_question_fk != null)
            return false;
        if (id_survey_fk != null ? !id_survey_fk.equals(value1.id_survey_fk) : value1.id_survey_fk != null)
            return false;
        if (conflict != null ? !conflict.equals(value1.conflict) : value1.conflict != null)
            return false;
        if (upload_date != null ? !upload_date.equals(value1.upload_date) : value1.upload_date != null)
            return false;
        return !(id_option_fk != null ? !id_option_fk.equals(value1.id_option_fk) : value1.id_option_fk != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_value ^ (id_value >>> 32));
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (id_question_fk != null ? id_question_fk.hashCode() : 0);
        result = 31 * result + (id_survey_fk != null ? id_survey_fk.hashCode() : 0);
        result = 31 * result + (id_option_fk != null ? id_option_fk.hashCode() : 0);
        result = 31 * result + (upload_date != null ? upload_date.hashCode() : 0);
        result = 31 * result + (conflict != null ? conflict.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String uploadDateAsString= (upload_date!=null)?upload_date.toString():"";
        return "Value{" +
                "id_value=" + id_value +
                ", value='" + value + '\'' +
                ", id_question=" + id_question_fk +
                ", id_survey=" + id_survey_fk +
                ", id_option=" + id_option_fk +
                ", upload_date=" + uploadDateAsString +
                ", conflict=" + conflict +
                '}';
    }

}
