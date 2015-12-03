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
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;

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
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_answer",
            columnType = Long.class,
            foreignColumnName = "id_answer")},
            saveForeignKeyModel = false)
    Answer answer;

    @Column
    String path;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_option_attribute",
            columnType = Long.class,
            foreignColumnName = "id_option_attribute")},
            saveForeignKeyModel = false)
    OptionAttribute optionAttribute;

    @Column
    String background_colour;
    List<Value> values;

    public Option() {
    }

    public Option(String name, Float factor, Answer answer) {
        this.name = name;
        this.factor = factor;
        this.answer = answer;
    }

    public Option(String code, String name, Float factor, Answer answer) {
        this.name = name;
        this.factor = factor;
        this.answer = answer;
        this.code = code;
    }

    public Option(String name, Float factor, Answer answer, String code, OptionAttribute optionAttribute, String background_colour) {
        this.name = name;
        this.factor = factor;
        this.answer = answer;
        this.code = code;
        this.optionAttribute = optionAttribute;
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

    public String getCode() {return code;}

    public void setCode(String code) {this.code = code;}

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
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public OptionAttribute getOptionAttribute() {
        return optionAttribute;
    }

    public void setOptionAttribute(OptionAttribute optionAttribute) {
        this.optionAttribute = optionAttribute;
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

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "values")
    public List<Value> getValues(){
        return new Select().from(Value.class)
                .where(Condition.column(Value$Table.OPTION_ID_OPTION).eq(this.getId_option())).queryList();
    }

    /**
     * Returns a copy of this option
     * @return
     */
    public Option copy(){
        Option optionCopy=new Option(code,name,factor,answer);
        optionCopy.save();
        return optionCopy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Option)) return false;

        Option option = (Option) o;

        if (id_option != option.id_option) return false;
        if (!name.equals(option.name)) return false;
        if (factor != null ? !factor.equals(option.factor) : option.factor != null) return false;
        return answer.equals(option.answer);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_option ^ (id_option >>> 32));
        result = 31 * result + code.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (factor != null ? factor.hashCode() : 0);
        result = 31 * result + answer.hashCode();
        result = 31 * result + (optionAttribute != null ? optionAttribute.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (background_colour != null ? background_colour.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Option{" +
                "id=" + id_option +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", factor=" + factor +
                ", answer=" + answer +
                ", path=" + path +
                ", optionAttribute=" + optionAttribute +
                ", background_colour=" + background_colour +
                '}';
    }
}
