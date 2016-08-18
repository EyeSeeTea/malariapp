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

package org.eyeseetea.malariacare.layout.dashboard.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.eyeseetea.malariacare.layout.dashboard.deserializers.DatabaseOriginTypeDeserializer;

/**
 * POJO that tells how to populate the db
 * Created by arrizabalaga on 16/03/16.
 */
public class DatabaseSettings {

    /**
     * Type of origin for the metadata
     */
    @JsonDeserialize(using = DatabaseOriginTypeDeserializer.class)
    DatabaseOriginType originType;

    /**
     * Uri that links to the metadata (csv path, default dhis server, ..)
     */
    String uri;

    /**
     * Indicates if the sdk should retrieve the full hierarchy of orgUnits or only its leaves
     */
    boolean fullHierarchy;

    DatabaseSettings(){

    }

    public DatabaseOriginType getOriginType() {
        return originType;
    }

    public void setOriginType(DatabaseOriginType originType) {
        this.originType = originType;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isFullHierarchy(){
        return fullHierarchy;
    }

    public void setFullHierarchy(boolean hierarchy){
        this.fullHierarchy =hierarchy;
    }

    @Override
    public String toString() {
        return "DatabaseSettings{" +
                "originType=" + originType +
                ", uri='" + uri + '\'' +
                ", fullHierarchy=" + fullHierarchy +
                '}';
    }
}
