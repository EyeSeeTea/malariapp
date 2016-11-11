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
    public static final String compositeScoreName = "cs";
    public static final String compositeScoreTwoName = "cs2";
    public static final String programName = "pg";
    public static final String orgUnitProgramRelationName = "oupr";

    public static final NameAlias questionAlias = NameAlias.builder(questionName).build();
    public static final NameAlias questionRelationAlias = NameAlias.builder(
            questionRelationName).build();
    public static final NameAlias questionOptionAlias = NameAlias.builder(
            questionOptionName).build();
    public static final NameAlias valueAlias = NameAlias.builder(valueName).build();
    public static final NameAlias matchAlias = NameAlias.builder(matchName).build();
    public static final NameAlias headerAlias = NameAlias.builder(headerName).build();
    public static final NameAlias tabAlias = NameAlias.builder(tabName).build();
    public static final NameAlias compositeScoreAlias = NameAlias.builder(
            compositeScoreName).build();
    public static final NameAlias compositeScoreTwoAlias = NameAlias.builder(
            compositeScoreTwoName).build();
    public static final NameAlias programAlias = NameAlias.builder(programName).build();
    public static final NameAlias orgUnitProgramRelationAlias = NameAlias.builder(
            orgUnitProgramRelationName).build();


    public static final String programFlowName = "pgf";
    public static final String programStageFlowName = "psf";
    public static final String programStageSectionFlowName = "pssf";
    public static final String programStageDataElementFlowName = "psdef";

    public static final NameAlias programFlowAlias = NameAlias.builder(programFlowName).build();
    public static final NameAlias programStageFlowAlias = NameAlias.builder(
            programStageFlowName).build();
    public static final NameAlias programStageSectionFlowAlias = NameAlias.builder(
            programStageSectionFlowName).build();
    public static final NameAlias programStageDataElementFlowAlias = NameAlias.builder(
            programStageDataElementFlowName).build();

}
