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

package org.eyeseetea.malariacare.data.database.model;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

@Table(database = AppDatabase.class)
public class ServerMetadata extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_server_metadata;

    @Column
    String name;

    @Column
    String code;

    @Column
    String uid_server_metadata;

    @Column
    String value_type;

    public ServerMetadata() {
    }

    public ServerMetadata(long id_control_dataelement, String name, String code, String valueType, String uid) {
        this.id_server_metadata = id_control_dataelement;
        this.name = name;
        this.code = code;
        this.uid_server_metadata = uid;
        this.value_type = valueType;
    }

    public long getId_server_metadata() {
        return id_server_metadata;
    }

    public void setId_server_metadata(long id_control_dataelement) {
        this.id_server_metadata = id_control_dataelement;
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
        return uid_server_metadata;
    }

    public void setUid(String uid) {
        this.uid_server_metadata = uid;
    }

    public String getValueType() {
        return value_type;
    }

    public void setValueType(String value_type) {
        this.value_type = value_type;
    }

    public static ServerMetadata findControlDataElementByCode(String code){
        return new Select().from(ServerMetadata.class).where(ServerMetadata_Table.code.eq(code)).querySingle();
    }

    public static ServerMetadata findControlDataElementByName(String code){
        return new Select().from(ServerMetadata.class).where(ServerMetadata_Table.name.eq(code)).querySingle();
    }

    public static String findControlDataElementUid(String code){
        ServerMetadata controlDataElement=findControlDataElementByCode(code);
        if(controlDataElement==null){
            controlDataElement=findControlDataElementByName(code);
        }
        if(controlDataElement==null){
            Log.d(".ServerMetadata", String.format("WARNING: Control DE with code %s not found, ignoring", code));
            return null;
        }
        return controlDataElement.getUid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerMetadata that = (ServerMetadata) o;

        if (id_server_metadata != that.id_server_metadata) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (uid_server_metadata != null ? !uid_server_metadata.equals(that.uid_server_metadata) : that.uid_server_metadata != null) return false;
        return !(value_type != null ? !value_type.equals(that.value_type) : that.value_type != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_server_metadata ^ (id_server_metadata >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (value_type != null ? value_type.hashCode() : 0);
        result = 31 * result + (uid_server_metadata != null ? uid_server_metadata.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServerMetadata{" +
                "id_control_dataelement=" + id_server_metadata +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", uid='" + uid_server_metadata + '\'' +
                ", value_type=" + value_type +
                '}';
    }
}