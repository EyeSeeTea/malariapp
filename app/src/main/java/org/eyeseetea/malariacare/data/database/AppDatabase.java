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

package org.eyeseetea.malariacare.data.database;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

/**
 * Created by nacho on 02/08/15.
 */
@Database(
        name = AppDatabase.NAME, version = AppDatabase.VERSION
)

public class AppDatabase {
    public static final String NAME = "EyeSeeTeaDB";
    public static final int VERSION = 21;

    // Aliases used for EyeSeeTea DB queries
    public static final String obsActionPlanName = "oap";
    public static final String surveyName = "s";
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
    public static final String orgUnitName = "ou";
    public static final String orgUnitProgramRelationName = "oupr";
    public static final String surveyProgramRelationName = "spr";

    public static final NameAlias surveyAlias = NameAlias.builder(surveyName).build();
    public static final NameAlias obsActionPlanAlias = NameAlias.builder(obsActionPlanName).build();
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
    public static final NameAlias surveyProgramRelationAlias = NameAlias.builder(
            surveyProgramRelationName).build();
    public static final NameAlias orgUnitAlias = NameAlias.builder(orgUnitName).build();


    // Aliases used in DHIS2 DB wrappers
    public static final String programFlowName = "pgf";
    public static final String programStageFlowName = "psf";
    public static final String programStageSectionFlowName = "pssf";
    public static final String programStageDataElementFlowName = "psdef";
    public static final String programAttributeFlowName = "paf";
    public static final String attributeFlowName = "af";
    public static final String optionFlowName = "onf";
    public static final String attributeValueFlowName = "avf";

    public static final NameAlias programFlowAlias = NameAlias.builder(programFlowName).build();
    public static final NameAlias programStageFlowAlias = NameAlias.builder(
            programStageFlowName).build();
    public static final NameAlias programStageSectionFlowAlias = NameAlias.builder(
            programStageSectionFlowName).build();
    public static final NameAlias programStageDataElementFlowAlias = NameAlias.builder(
            programStageDataElementFlowName).build();
    public static final NameAlias programAttributeFlowAlias = NameAlias.builder(
            programAttributeFlowName).build();
    public static final NameAlias attributeFlowAlias = NameAlias.builder(attributeFlowName).build();
    public static final NameAlias optionFlowAlias = NameAlias.builder(optionFlowName).build();
    public static final NameAlias attributeValueFlowAlias = NameAlias.builder(
            attributeValueFlowName).build();
}