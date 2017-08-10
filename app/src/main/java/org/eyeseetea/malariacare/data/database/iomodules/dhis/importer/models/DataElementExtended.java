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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models;

import static org.eyeseetea.malariacare.data.database.AppDatabase.programFlowAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programFlowName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageDataElementFlowAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageDataElementFlowName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageFlowAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageFlowName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageSectionFlowAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programStageSectionFlowName;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.remote.SdkQueries;
import org.eyeseetea.malariacare.utils.AUtils;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeValueFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionSetFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow_Table;
import org.hisp.dhis.client.sdk.models.dataelement.ValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class DataElementExtended implements VisitableFromSDK {

    private static final String TAG = ".DataElementExtended";

    /**
     * Code of attribute dheader unique name
     */
    public static final String ATTRIBUTE_HEADER_NAME = "DEHeader";
    /**
     * Code of attribute order int
     */
    public static final String ATTRIBUTE_ORDER = "Order";
    /**
     * Code of attribute numerator float
     */
    public static final String ATTRIBUTE_NUMERATOR = "DENumerator";
    /**
     * Code of attribute denominator float
     */
    public static final String ATTRIBUTE_DENUMERATOR = "DEDenominator";
    /**
     * Code of attribute of parent options separated by token
     */
    public static final String ATTRIBUTE_PARENT_QUESTION_OPTIONS = "DEQuestionParentOptions";
    /**
     * Code of attribute of parents separated by token
     */
    public static final String ATTRIBUTE_PARENT_QUESTION = "DEQuestionParents";
    /**
     * Code of attribute of group of patern/child relation
     */
    public static final String ATTRIBUTE_HIDE_GROUP = "DEHideGroup";
    /**
     * Code of attribute of type patern or child
     */
    public static final String ATTRIBUTE_HIDE_TYPE = "DEHideType";
    /**
     * Code of attribute of Match group of patern/child relation
     */
    public static final String ATTRIBUTE_MATCH_GROUP = "DEMatchGroup";
    /**
     * Code of attribute of Match type patern or child
     */
    public static final String ATTRIBUTE_MATCH_TYPE = "DEMatchType";

    /**
     * Code of attribute 19 DE Type  (Question, Control, Score)
     */
    public static final String ATTRIBUTE_ELEMENT_TYPE_CODE = "DEType";

    /**
     * Code the attribute Row (for customTabs)
     */
    public static final String ATTRIBUTE_ROW = "DERow";

    /**
     * Code the attribute Column (for customTabs)
     */
    public static final String ATTRIBUTE_COLUMN = "DEColumn";

    /**
     * Code the attribute Video
     */
    public static final String ATTRIBUTE_VIDEO = "DEVideo";

    /**
     * Code the attribute Image
     */
    public static final String ATTRIBUTE_IMAGE = "DEImage";

    /**
     * Code of Question option for attribute DEType
     */
    public static String OPTION_ELEMENT_TYPE_QUESTION_CODE = null;

    /**
     * Code of Control option for attribute DEType
     */
    public static String OPTION_ELEMENT_TYPE_CONTROL_CODE = null;

    /**
     * Code of Composite option for attribute DEType
     */
    public static String OPTION_ELEMENT_TYPE_SCORE_CODE = null;

    /**
     * Code of attribute 'Composite Score'
     */
    public static final String ATTRIBUTE_COMPOSITE_SCORE_CODE = "DECompositiveScore";
    /**
     * Value to discard the dataelementcontrol composite
     */
    public static final String OPTION_COMPOSITE_SCORE_NAME = "COMPOSITE_SCORE";

    /**
     * Value to discard the dataelementcontrol question
     */
    public static final String OPTION_QUESTION_NAME = "QUESTION";

    /**
     * Value to discard the dataelementcontrol control
     */
    public static final String OPTION_CONTROL_NAME = "CONTROL_DATAELEMENT";

    /**
     * Value parent
     */
    public static final String PARENT = "PARENT";
    /**
     * Value child
     */
    public static final String CHILD = "CHILD";

    DataElementFlow dataElement;

    String programUid;


    public DataElementExtended(DataElementFlow dataElement) {
        this.dataElement = dataElement;
    }

    public DataElementExtended(DataElementExtended dataElement) {
        this.dataElement = dataElement.getDataElementFlow();
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public String getCode() {
        return dataElement.getCode();
    }

    public String getUid() {
        return dataElement.getUId();
    }

    private List<AttributeValueFlow> getAttributeValues() {
        return dataElement.getAttributeValueFlow();
    }

    /**
     * Reloads the codes for the options Question, Control, Score
     */
    public static void reloadDataElementTypeCodes() {
        //Load code for each type
        OptionSetFlow deTypeOptionSet = OptionSetExtended.findOptionSetForDataElementType();
        if (deTypeOptionSet == null) {
            Log.e(TAG,
                    "No optionset with 'DB - DE Type', dataElements will not be loaded correctly");
            return;
        }
        String optionSetUID = deTypeOptionSet.getUId();

        //Reload codes for score, question and control
        OPTION_ELEMENT_TYPE_QUESTION_CODE = loadDataElementTypeCode(optionSetUID,
                OPTION_QUESTION_NAME);
        OPTION_ELEMENT_TYPE_CONTROL_CODE = loadDataElementTypeCode(optionSetUID,
                OPTION_CONTROL_NAME);
        OPTION_ELEMENT_TYPE_SCORE_CODE = loadDataElementTypeCode(optionSetUID,
                OPTION_COMPOSITE_SCORE_NAME);
    }

    /**
     * Returns the option code for a given optionSet and option name
     */
    private static String loadDataElementTypeCode(String optionSetUID, String optionName) {
        OptionFlow option = OptionExtended.findOptionByOptionSetAndName(optionSetUID, optionName);
        if (option == null) {
            Log.e(TAG,
                    String.format("No option with '%s', dataElements will not be loaded correctly",
                            optionName));
            return null;
        }
        return option.getCode();
    }


    public DataElementExtended getDataElement() {
        return new DataElementExtended(dataElement);
    }

    public DataElementFlow getDataElementFlow() {
        return dataElement;
    }

    /**
     * Gets value in the AttributeValue table
     *
     * @return value
     */
    public String getValue(String attributeCode) {
        AttributeValueFlow attributeValue =
                AttributeValueExtended.findAttributeValuefromDataElementCode(attributeCode,
                        getAttributeValues());
        if (attributeValue != null) {
            return attributeValue.getValue();
        }
        return null;
    }

    /**
     * Checks if this dataElement is a CompositeScoreDB
     */
    public boolean isCompositeScore() {
        return isOfType(OPTION_ELEMENT_TYPE_SCORE_CODE);
    }

    /**
     * Checks if this dataElement is a CompositeScoreDB
     */
    public boolean isControlDataElement() {
        return isOfType(OPTION_ELEMENT_TYPE_CONTROL_CODE);
    }

    /**
     * Checks if this dataElement is a CompositeScoreDB
     */
    public boolean isQuestion() {
        return isOfType(OPTION_ELEMENT_TYPE_QUESTION_CODE);
    }

    /**
     * Returns if this dataElement has an attribute with code 'DEType' whose value matches the
     * given
     * value
     */
    private boolean isOfType(String optionElementTypeCode) {
        String typeElement = AttributeValueExtended.findAttributeValueByCode(
                ATTRIBUTE_ELEMENT_TYPE_CODE, getAttributeValues());

        if (typeElement == null) {
            Log.w(TAG,
                    String.format("DataElement '%s' type cannot be solved, no attribute for '%s'",
                            getCode(), ATTRIBUTE_ELEMENT_TYPE_CODE));
            return false;
        }

        return typeElement.equals(optionElementTypeCode);
    }


    /**
     * Find the associated programStage (tabgroup)
     */
    public String findProgramUID() {
        return DataElementExtended.findProgramUIDByDataElementUID(dataElement.getUId());
    }

    /**
     * Find the associated program given a dataelement UID
     */
    public static String findProgramUIDByDataElementUID(String dataElementUID) {
        //Find the right 'uid' of the dataelement program
        ProgramFlow program = new Select().from(ProgramFlow.class).as(programFlowName)
                .join(ProgramStageFlow.class, Join.JoinType.LEFT_OUTER).as(programStageFlowName)
                .on(ProgramFlow_Table.uId.withTable(programFlowAlias)
                        .eq(ProgramStageFlow_Table.program.withTable(programStageFlowAlias)))
                .join(ProgramStageDataElementFlow.class, Join.JoinType.LEFT_OUTER).as(
                        programStageDataElementFlowName)
                .on(ProgramStageDataElementFlow_Table.programStage.withTable(
                        programStageDataElementFlowAlias)
                        .eq(ProgramStageFlow_Table.uId.withTable(programStageFlowAlias)))
                .where(ProgramStageDataElementFlow_Table.dataElement.withTable(
                        programStageDataElementFlowAlias).eq(dataElementUID))
                .querySingle();
        if (program == null) {
            return null;
        }
        return program.getUId();
    }

    /**
     * Find the associated programStageSection (tab)UID given a dataelement UID
     */
    public static String findProgramStageSectionUIDByDataElementUID(String dataElementUID) {
        //Find the right 'uid' of the dataelement program
        ProgramStageSectionFlow programSS = new Select().from(ProgramStageSectionFlow.class).as(
                programStageSectionFlowName)
                .join(ProgramStageDataElementFlow.class, Join.JoinType.LEFT_OUTER).as(
                        programStageDataElementFlowName)
                .on(ProgramStageSectionFlow_Table.uId.withTable(programStageSectionFlowAlias)
                        .eq(ProgramStageDataElementFlow_Table.programStageSection.withTable(
                                programStageDataElementFlowAlias)))
                .where(ProgramStageDataElementFlow_Table.dataElement.withTable(
                        programStageDataElementFlowAlias).eq(dataElementUID))
                .querySingle();
        if (programSS == null) {
            return null;
        }
        return programSS.getUId();
    }


    /**
     * Find the associated programStageSection (tab)UID given a dataelement UID
     */
    public String findProgramStageSection() {
        List<ProgramStageDataElementFlow> programStageDataElementFlows =
                new Select().from(ProgramStageDataElementFlow.class)
                        .where(ProgramStageDataElementFlow_Table.dataElement.eq(getUid()))
                        .groupBy(ProgramStageDataElementFlow_Table.programStage).queryList();

        for (ProgramStageDataElementFlow programStageDataElementFlow :
                programStageDataElementFlows) {
            ProgramStageSectionFlow programStageSection =
                    programStageDataElementFlow.getProgramStageSection();
            if (SdkQueries.getProgramStage(
                    programStageSection.getProgramStage()).getProgram().getUId().equals(
                    programUid)) {
                return programStageSection.getUId();
            }
        }
        return null;
    }

    /**
     * Find the associated ProgramStageDataElement (tab) given a DataElementExtended
     */
    public static ProgramStageDataElementFlow findProgramStageDataElementByDataElementExtended(
            DataElementExtended dataElementExtended) {
        String dataElementUID = dataElementExtended.getUid();
        String programUID = dataElementExtended.getProgramUid();
        //Find the right 'uid' of the dataelement program
        List<ProgramStageDataElementFlow> programDES = new Select().from(
                ProgramStageDataElementFlow.class).as(programStageDataElementFlowName)
                .where(ProgramStageDataElementFlow_Table.dataElement.withTable(
                        programStageDataElementFlowAlias).eq(dataElementUID))
                .queryList();
        if (programDES == null) {
            return null;
        }
        for (ProgramStageDataElementFlow programStageDataElement : programDES) {
            if (SdkQueries.getProgramStage(
                    programStageDataElement.getProgramStage()).getProgram().getUId().equals(
                    programUID)) {
                return programStageDataElement;
            }
        }
        return null;
    }

    /**
     * Find the associated ProgramStageDataElement (tab) given a dataelement UID
     */
    public static ProgramStageDataElementFlow findProgramStageDataElementByDataElementUID(
            String dataElementUID) {
        //Find the right 'uid' of the dataelement program
        ProgramStageDataElementFlow programDE = new Select().from(
                ProgramStageDataElementFlow.class).as(programStageDataElementFlowName)
                .where(ProgramStageDataElementFlow_Table.dataElement.withTable(
                        programStageDataElementFlowAlias).eq(dataElementUID))
                .querySingle();
        if (programDE == null) {
            return null;
        }
        return programDE;
    }

    /**
     * Find the order from dataelement in programStage
     */
    public static String findProgramStageSectionOrderDataElementOrderByDataElementUID(
            String dataElementUID) {
        //Find the right 'uid' of the dataelement program
        ProgramStageSectionFlow programSS = new Select().from(ProgramStageSectionFlow.class).as(
                programStageSectionFlowName)
                .join(ProgramStageDataElementFlow.class, Join.JoinType.LEFT_OUTER).as(
                        programStageDataElementFlowName)
                .on(ProgramStageDataElementFlow_Table.programStageSection.withTable(
                        programStageDataElementFlowAlias)
                        .eq(ProgramStageSectionFlow_Table.uId
                                .withTable(programStageSectionFlowAlias)))
                .where(ProgramStageDataElementFlow_Table.dataElement.withTable(
                        programStageDataElementFlowAlias).eq(dataElementUID))
                .querySingle();
        if (programSS == null) {
            return null;
        }
        return programSS.getSortOrder() + "";
    }

    public Integer findOrder() {
        String value = getValue(ATTRIBUTE_ORDER);
        if (value != null) {
            int order = Integer.valueOf(value);
            return order;
        }
        return null;
    }

    public String findCompositeScoreId() {
        return getValue(ATTRIBUTE_COMPOSITE_SCORE_CODE);
    }

    public Float findNumerator() {
        String value = getValue(ATTRIBUTE_NUMERATOR);
        if (value != null && !value.equals("")) {
            float numerator = AUtils.safeParseFloat(value);
            return numerator;
        } else {
            return findDenominator();
        }
    }

    public Float findDenominator() {
        String value = getValue(ATTRIBUTE_DENUMERATOR);
        if (value != null && !value.equals("")) {
            float denominator = AUtils.safeParseFloat(value);
            return denominator;
        }
        return 0.0f;
    }

    public Integer findRow() {
        String value = getValue(ATTRIBUTE_ROW);
        if (value != null) {
            int row = Integer.valueOf(value);
            return row;
        }
        return null;
    }

    public Integer findColumn() {
        String value = getValue(ATTRIBUTE_COLUMN);
        if (value != null) {
            int row = Integer.valueOf(value);
            return row;
        }
        return null;
    }

    public CompositeScoreDB findCompositeScore() {
        CompositeScoreDB compositeScore = null;

        String value = findCompositeScoreId();
        if (value != null) {
            try {
                compositeScore =
                        CompositeScoreBuilder.getCompositeScoreFromDataElementAndHierarchicalCode(
                                this, getProgramUid(), value);
            } catch (Exception e) {
                return compositeScore;
            }
        }
        return compositeScore;
    }

    public String getProgramUid() {
        return programUid;
    }

    public void setProgramUid(String programUid) {
        this.programUid = programUid;
    }

    public static boolean existsDataElementByUid(String uid) {
        int result = (int) new SQLite().selectCountOf().from(DataElementFlow.class)
                .where(DataElementFlow_Table.uId.is(uid)).count();
        Log.d(TAG, "dataelement " + uid + " count: " + result);
        return (result > 0);
    }

    public String getName() {
        return dataElement.getName();
    }

    public String getShortName() {
        return dataElement.getDisplayName();
    }

    public String getFormName() {
        return dataElement.getFormName();
    }

    public String getDescription() {
        return dataElement.getDescription();
    }

    public String getOptionSet() {
        if (dataElement.getOptionSet() == null) {
            return null;
        }
        return dataElement.getOptionSet().getUId();
    }

    public String getDisplayName() {
        return dataElement.getDisplayName();
    }

    public ValueType getValueType() {
        return dataElement.getValueType();
    }

    public static List<DataElementExtended> getExtendedList(List<DataElementFlow> flowList) {
        List<DataElementExtended> extendedsList = new ArrayList<>();
        for (DataElementFlow flowPojo : flowList) {
            extendedsList.add(new DataElementExtended(flowPojo));
        }
        return extendedsList;
    }
}
