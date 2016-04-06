/*
 * Copyright (c) 2016.
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
import org.eyeseetea.malariacare.database.utils.Session;

/**
 * Created by idelcano on 05/04/2016.
 */

@Table(databaseName = AppDatabase.NAME)
public class ControlDataElement extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_control_dataelement;

    @Column
    String name;

    @Column
    String code;

    @Column
    String uid;

    @Column
    String valueType;

    public ControlDataElement() {
    }

    public ControlDataElement(long id_control_dataelement, String name, String code, String valueType, String uid) {
        this.id_control_dataelement = id_control_dataelement;
        this.name = name;
        this.code = code;
        this.uid = uid;
        this.valueType = valueType;
    }

    public long getId_control_dataelement() {
        return id_control_dataelement;
    }

    public void setId_control_dataelement(long id_control_dataelement) {
        this.id_control_dataelement = id_control_dataelement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ControlDataElement that = (ControlDataElement) o;

        if (id_control_dataelement != that.id_control_dataelement) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;
        return !(valueType != null ? !valueType.equals(that.valueType) : that.valueType != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_control_dataelement ^ (id_control_dataelement >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ControlDataElement{" +
                "id_control_dataelement=" + id_control_dataelement +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", uid='" + uid + '\'' +
                ", valueType=" + valueType +
                '}';
    }

    public static ControlDataElement findControlDataElementByCode(String code){
        return new Select().from(ControlDataElement.class).where(Condition.column(ControlDataElement$Table.CODE).eq(code)).querySingle();
    }

    public static ControlDataElement findControlDataElementByName(String code){
        return new Select().from(ControlDataElement.class).where(Condition.column(ControlDataElement$Table.NAME).eq(code)).querySingle();
    }

    public static String findControlDataElementUid(String code){
        ControlDataElement controlDataElement=findControlDataElementByCode(code);
        if(controlDataElement==null){
            controlDataElement=findControlDataElementByName(code);
        }
        return controlDataElement.getUid();
    }
}