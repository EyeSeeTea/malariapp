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
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

import java.util.List;

@Table(database = AppDatabase.class, name = "Server")
public class ServerDB extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id_server;

    @Column
    String name;

    @Column
    String url;

    @Column
    Blob logo;

    @Column
    boolean connected;

    @Column
    Integer classification;

    public long getId_server() {
        return id_server;
    }

    public void setId_server(long id_server) {
        this.id_server = id_server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Blob getLogo() {
        return logo;
    }

    public void setLogo(Blob logo) {
        this.logo = logo;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    // Query method are here instead of ServerLocalDataSource because Kapt plugin
    // launch error to build for DbFlow 3.0.1
    // "Could not resolve all files for configuration ':app:kapt'
    public static ServerDB getConnectedServerFromDB() {
        return new Select().from(ServerDB.class)
                .where(ServerDB_Table.connected.is(true)).querySingle();
    }

    public static List<ServerDB> getAllServersFromDB() {
        return new Select().from(ServerDB.class).queryList();
    }

    public static ServerDB getServerFromDByUrl(String url) {
        return new Select().from(ServerDB.class)
                .where(ServerDB_Table.url.eq(url)).querySingle();
    }

    public Integer getClassification() {
        return classification;
    }

    public void setClassification(Integer classification) {
        this.classification = classification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerDB serverDB = (ServerDB) o;

        if (id_server != serverDB.id_server) return false;
        if (connected != serverDB.connected) return false;
        if (!name.equals(serverDB.name)) return false;
        if (!url.equals(serverDB.url)) return false;
        if (!logo.equals(serverDB.logo)) return false;
        return classification.equals(serverDB.classification);
    }

    @Override
    public int hashCode() {
        int result = (int) (id_server ^ (id_server >>> 32));
        result = 31 * result + name.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + logo.hashCode();
        result = 31 * result + (connected ? 1 : 0);
        result = 31 * result + classification.hashCode();
        return result;
    }
}
