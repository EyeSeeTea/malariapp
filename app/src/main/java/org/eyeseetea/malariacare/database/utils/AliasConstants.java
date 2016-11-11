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

package org.eyeseetea.malariacare.database.utils;

import com.raizlabs.android.dbflow.sql.language.NameAlias;

/**
 * Created by ina on 11/11/2016.
 */

public class AliasConstants {

    public static final String matchName = "m";
    public static final String valueName = "v";
    public static final String questionName = "q";
    public static final String questionRelationName = "qr";
    public static final String questionOptionName = "qo";
    public static final String headerName = "h";
    public static final String tabName = "t";


    public static final NameAlias questionAlias = NameAlias.builder(questionName).build();
    public static final NameAlias questionRelationAlias = NameAlias.builder(
            questionRelationName).build();
    public static final NameAlias questionOptionAlias = NameAlias.builder(
            questionOptionName).build();
    public static final NameAlias valueAlias = NameAlias.builder(valueName).build();
    public static final NameAlias matchAlias = NameAlias.builder(matchName).build();
    public static final NameAlias headerAlias = NameAlias.builder(headerName).build();
    public static final NameAlias tabAlias = NameAlias.builder(tabName).build();

}
