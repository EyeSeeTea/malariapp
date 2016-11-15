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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer.models;

import static org.eyeseetea.malariacare.database.utils.AliasConstants.programFlowAlias;
import static org.eyeseetea.malariacare.database.utils.AliasConstants.programFlowName;
import static org.eyeseetea.malariacare.database.utils.AliasConstants
        .programStageDataElementFlowAlias;
import static org.eyeseetea.malariacare.database.utils.AliasConstants
        .programStageDataElementFlowName;
import static org.eyeseetea.malariacare.database.utils.AliasConstants.programStageFlowAlias;
import static org.eyeseetea.malariacare.database.utils.AliasConstants.programStageFlowName;
import static org.eyeseetea.malariacare.database.utils.AliasConstants.programStageSectionFlowAlias;
import static org.eyeseetea.malariacare.database.utils.AliasConstants.programStageSectionFlowName;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.sdk.SdkController;
import org.eyeseetea.malariacare.sdk.models.AttributeFlow;
import org.eyeseetea.malariacare.sdk.models.AttributeValueFlow;
import org.eyeseetea.malariacare.utils.AUtils;
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
        // TODO: 15/11/2016
        //return dataElement.getCode()
        return null;
    }

    public String getUid() {
        return dataElement.getUId();
    }

    private List<AttributeValueFlow> getAttributeValues() {
        //// TODO: 15/11/2016
        //dataElement.getAttributeValues();
        return null;
    }

    /**
     * Reloads the codes for the options Question, Control, Score
     */
    public static void reloadDataElementTypeCodes() {
        //Load code for each type
        //// FIXME: 11/11/2016
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
        //// FIXME: 11/11/2016
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
        AttributeValueFlow attributeValue = findAttributeValuefromDataElementCode(attributeCode,
                getDataElementFlow());
        if (attributeValue != null) {
            return attributeValue.getValue();
        }
        return null;
    }

    /**
     * Checks if this dataElement is a CompositeScore
     */
    public boolean isCompositeScore() {
        return isOfType(OPTION_ELEMENT_TYPE_SCORE_CODE);
    }

    /**
     * Checks if this dataElement is a CompositeScore
     */
    public boolean isControlDataElement() {
        return isOfType(OPTION_ELEMENT_TYPE_CONTROL_CODE);
    }

    /**
     * Checks if this dataElement is a CompositeScore
     */
    public boolean isQuestion() {
        return isOfType(OPTION_ELEMENT_TYPE_QUESTION_CODE);
    }

    /**
     * Returns if this dataElement has an attribute with code 'DEType' whose value matches the given
     * value
     */
    private boolean isOfType(String optionElementTypeCode) {
        String typeElement = findAttributeValueByCode(ATTRIBUTE_ELEMENT_TYPE_CODE);

        if (typeElement == null) {
            Log.w(TAG,
                    String.format("DataElement '%s' type cannot be solved, no attribute for '%s'",
                            getCode(), ATTRIBUTE_ELEMENT_TYPE_CODE));
            return false;
        }

        return typeElement.equals(optionElementTypeCode);
    }

    /**
     * Find the attributevalue in a dataelement for the given attribute
     */
    public AttributeValueFlow findAttributeValue(AttributeFlow attribute) {

        for (AttributeValueFlow attributeValue : getAttributeValues()) {
            if (attributeValue.getAttribute().getUId().equals(attribute.getUId())) {
                return attributeValue;
            }
        }
        return null;
    }


    /**
     * Finds the value of an attribute with the given code in a dataElement
     */
    public String findAttributeValueByCode(String code) {

        //Find the right attribute
        AttributeFlow attribute = AttributeExtended.findAttributeByCode(code);
        //No such attribute -> done
        if (attribute == null) {
            Log.d("DataElementExtended",
                    String.format("findAttributeByCode(): Attribute with %s not found", code));
            return null;
        }

        //Find its value for the given dataelement
        AttributeValueFlow attributeValue = findAttributeValue(attribute);
        if (attributeValue == null) {
            return null;
        }
        return attributeValue.getValue();
    }

    /**
     * Find the attribute in a dataelement for the given code
     */
    public AttributeValueFlow findAttributeValuefromDataElementCode(String code,
            DataElementFlow dataElement) {
        if (code == null || dataElement == null) {
            return null;
        }
        //select * from Attribute join AttributeValue on Attribute.id = attributeValue
        // .attributeId join DataElementAttributeValue on attributeValue
        // .id=DataElementAttributeValue.attributeValueId where DataElementAttributeValue
        // .dataElementId="vWgsPN1RPLl" and code="Order"
        for (AttributeValueFlow attributeValue : getAttributeValues()) {
            if (attributeValue.getAttribute().getCode() == null) {
                throw new RuntimeException(String.format(
                        PreferencesState.getInstance().getContext().getResources().getString(
                                R.string.dialog_error_attribute_null),
                        attributeValue.getAttributeId()));
            }
            if (attributeValue.getAttribute().getCode().equals(code)) {
                return attributeValue;
            }
        }
        return null;
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
                .on(ProgramFlow_Table.id.withTable(programFlowAlias)
                        .eq(ProgramStageFlow_Table.program.withTable(programStageFlowAlias)))
                .join(ProgramStageDataElementFlow.class, Join.JoinType.LEFT_OUTER).as(
                        programStageDataElementFlowName)
                .on(ProgramStageDataElementFlow_Table.programStage.withTable(
                        programStageDataElementFlowAlias)
                        .eq(ProgramStageFlow_Table.id.withTable(programStageFlowAlias)))
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
                .on(ProgramStageSectionFlow_Table.id.withTable(programStageSectionFlowAlias)
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

        List<ProgramStageSectionFlow> programStageSections = new Select().from(
                ProgramStageSectionFlow.class).as(
                programStageSectionFlowName)
                .join(ProgramStageDataElementFlow.class, Join.JoinType.LEFT_OUTER).as(
                        programStageDataElementFlowName)
                .on(ProgramStageSectionFlow_Table.id.withTable(programStageSectionFlowAlias)
                        .eq(ProgramStageDataElementFlow_Table.programStageSection.withTable(
                                programStageDataElementFlowAlias)))
                .where(ProgramStageDataElementFlow_Table.dataElement.withTable(
                        programStageDataElementFlowAlias).eq(getUid()))
                .queryList();
        if (programStageSections == null) {
            return null;
        }
        for (ProgramStageSectionFlow programStageSection : programStageSections) {
            if (SdkController.getProgramStage(
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
            if (SdkController.getProgramStage(
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
                        .eq(ProgramStageSectionFlow_Table.id
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

    public CompositeScore findCompositeScore() {
        CompositeScore compositeScore = null;

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
        //// TODO: 15/11/2016 revise this:
        //return dataElement.getShortName;
        return dataElement.getDisplayName();
    }

    public String getFormName() {
        return dataElement.getFormName();
    }

    public String getDescription() {
        //// TODO: 15/11/2016 revise this:
        //return dataElement.getDescription;
        return dataElement.getDisplayFormName();
    }

    public String getOptionSet() {
        return dataElement.getOptionSet().getUId();
    }

    public String getDisplayName() {
        return dataElement.getDisplayName();
    }

    public ValueType getValueType() {
        return dataElement.getValueType();
    }

    public static List<DataElementExtended> getExtendedList(List<DataElementFlow> flowList) {
        List <DataElementExtended> extendedsList = new ArrayList<>();
        for(DataElementFlow flowPojo:flowList){
            extendedsList.add(new DataElementExtended(flowPojo));
        }
        return extendedsList;
    }
}
