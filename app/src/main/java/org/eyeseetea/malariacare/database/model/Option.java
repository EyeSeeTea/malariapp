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
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Option extends BaseModel {

    //FIXME A 'Yes' answer shows children questions, this should be configurable by some additional attribute in Option
    public static final String CHECKBOX_YES_OPTION="Yes";

    @Column
    @PrimaryKey(autoincrement = true)
    long id_option;
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

    @Column
    String path;

    @Column
    Long id_optionAttribute;
    
    /**
     * Reference to optionAttribute (loaded lazily)
     */
    OptionAttribute optionAttribute;

    @Column
    String background_colour;
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

    public Option(String name, Float factor, Answer answer, String code, OptionAttribute optionAttribute, String background_colour) {
        this.name = name;
        this.factor = factor;
        this.setAnswer(answer);
        this.code = code;
        this.setOptionAttribute(optionAttribute);
        this.background_colour = background_colour;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Answer getAnswer() {
        if(answer==null){
            if(id_answer==null) return null;
            answer = new Select()
                    .from(Answer.class)
                    .where(Condition.column(Answer$Table.ID_ANSWER)
                            .is(id_answer)).querySingle();
        }
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
        this.id_answer = (answer!=null)?answer.getId_answer():null;
    }

    public void setAnswer(Long id_answer){
        this.id_answer = id_answer;
        this.answer = null;
    }

    public OptionAttribute getOptionAttribute() {
        if(optionAttribute==null){
            if(id_optionAttribute==null) return null;
            optionAttribute = new Select()
                    .from(OptionAttribute.class)
                    .where(Condition.column(OptionAttribute$Table.ID_OPTION_ATTRIBUTE)
                            .is(id_optionAttribute)).querySingle();
        }
        return optionAttribute;
    }

    public void setOptionAttribute(OptionAttribute optionAttribute) {
        this.optionAttribute = optionAttribute;
        this.id_optionAttribute = (optionAttribute!=null)?optionAttribute.getId_option_attribute():null;
    }

    public void setOptionAttribute(Long id_optionAttribute){
        this.id_optionAttribute = id_optionAttribute;
        this.optionAttribute = null;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public String getBackground_colour() {
        return background_colour;
    }

    public void setBackground_colour(String background_colour) {
        this.background_colour = background_colour;
    }
    /**
     * Checks if this option actives the children questions
     * @return true: Children questions should be shown, false: otherwise.
     */
    public boolean isActiveChildren(){
        return CHECKBOX_YES_OPTION.equals(name);
    }

    /**
     * Checks if this option name is equals to a given string.
     *
     * @return true|false
     */
    public boolean is(String given){
        return given.equals(name);
    }

    public List<Value> getValues(){
        if(values==null){
            values = new Select().from(Value.class)
                    .where(Condition.column(Value$Table.ID_OPTION).eq(this.getId_option())).queryList();
        }
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        if (id_option != option.id_option) return false;
        if (code != null ? !code.equals(option.code) : option.code != null) return false;
        if (name != null ? !name.equals(option.name) : option.name != null) return false;
        if (factor != null ? !factor.equals(option.factor) : option.factor != null) return false;
        if (path != null ? !path.equals(option.path) : option.path != null) return false;
        if (id_optionAttribute != null ? !id_optionAttribute.equals(option.id_optionAttribute) : option.id_optionAttribute != null) return false;
        if (background_colour != null ? !background_colour.equals(option.background_colour) : option.background_colour != null) return false;
        return !(id_answer != null ? !id_answer.equals(option.id_answer) : option.id_answer != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_option ^ (id_option >>> 32));
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (factor != null ? factor.hashCode() : 0);
        result = 31 * result + (id_answer != null ? id_answer.hashCode() : 0);
        result = 31 * result + (id_optionAttribute != null ? id_optionAttribute.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (background_colour != null ? background_colour.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Option{" +
                "id_option=" + id_option +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", factor=" + factor +
                ", answer=" + answer +
                ", path=" + path +
                ", optionAttribute=" + optionAttribute +
                ", background_colour=" + background_colour +
                ", id_answer=" + id_answer +
                '}';
    }
}
