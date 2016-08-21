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

import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.hisp.dhis.android.sdk.persistence.models.Attribute;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.Program$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection$Table;

import java.util.EmptyStackException;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class DataElementExtended implements VisitableFromSDK {

    private static final String TAG=".DataElementExtended";

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

    DataElement dataElement;

    /**
     * Reloads the codes for the options Question, Control, Score
     */
    public static void reloadDataElementTypeCodes(){
        //Load code for each type
        OptionSet deTypeOptionSet = OptionSetExtended.findOptionSetForDataElementType();
        if(deTypeOptionSet==null){
            Log.e(TAG,"No optionset with 'DB - DE Type', dataElements will not be loaded correctly");
            return;
        }
        String optionSetUID=deTypeOptionSet.getUid();

        //Reload codes for score, question and control
        OPTION_ELEMENT_TYPE_QUESTION_CODE = loadDataElementTypeCode(optionSetUID,OPTION_QUESTION_NAME);
        OPTION_ELEMENT_TYPE_CONTROL_CODE = loadDataElementTypeCode(optionSetUID,OPTION_CONTROL_NAME);
        OPTION_ELEMENT_TYPE_SCORE_CODE = loadDataElementTypeCode(optionSetUID,OPTION_COMPOSITE_SCORE_NAME);
    }

    /**
     * Returns the option code for a given optionSet and option name
     * @param optionSetUID
     * @param optionName
     * @return
     */
    private static String loadDataElementTypeCode(String optionSetUID,String optionName){
        Option option = OptionExtended.findOptionByOptionSetAndName(optionSetUID,optionName);
        if(option==null){
            Log.e(TAG,String.format("No option with '%s', dataElements will not be loaded correctly",optionName));
            return null;
        }
        return option.getCode();
    }

    public DataElementExtended(){}

    public DataElementExtended(DataElement dataElement){
        this.dataElement =dataElement;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }


    public DataElement getDataElement() {
        return dataElement;
    }

    /**
     * Gets value in the AttributeValue table
     *
     * @param attributeCode
     * @return value
     */
    public String getValue(String attributeCode) {
        AttributeValue attributeValue = findAttributeValuefromDataElementCode(attributeCode, getDataElement());
        if (attributeValue != null) {
            return attributeValue.getValue();
        }
        return null;
    }

    /**
     * Checks if this dataElement is a CompositeScore
     * @return
     */
    public boolean isCompositeScore() {
        return isOfType(OPTION_ELEMENT_TYPE_SCORE_CODE);
    }

    /**
     * Checks if this dataElement is a CompositeScore
     * @return
     */
    public boolean isControlDataElement() {
        return isOfType(OPTION_ELEMENT_TYPE_CONTROL_CODE);
    }

    /**
     * Checks if this dataElement is a CompositeScore
     * @return
     */
    public boolean isQuestion() {
        return isOfType(OPTION_ELEMENT_TYPE_QUESTION_CODE);
    }

    /**
     * Returns if this dataElement has an attribute with code 'DEType' whose value matches the given value
     * @param optionElementTypeCode
     * @return
     */
    private boolean isOfType(String optionElementTypeCode){
        String typeElement = findAttributeValueByCode(ATTRIBUTE_ELEMENT_TYPE_CODE);

        if(typeElement==null){
            Log.w(TAG,String.format("DataElement '%s' type cannot be solved, no attribute for '%s'",dataElement.getCode(),ATTRIBUTE_ELEMENT_TYPE_CODE));
            return false;
        }

        return typeElement.equals(optionElementTypeCode);
    }

    /**
     * Find the attributevalue in a dataelement for the given attribute
     * @param attribute
     * @return
     */
    public AttributeValue findAttributeValue(Attribute attribute){

        for (AttributeValue attributeValue: getDataElement().getAttributeValues()){
            if (attributeValue.getAttribute().getUid().equals(attribute.getUid()))
                return attributeValue;
        }
        return null;
    }


    /**
     * Finds the value of an attribute with the given code in a dataElement
     * @param code
     * @return
     */
    public  String findAttributeValueByCode(String code){

        //Find the right attribute
        Attribute attribute = AttributeExtended.findAttributeByCode(code);
        //No such attribute -> done
        if(attribute==null){
            return null;
        }

        //Find its value for the given dataelement
        AttributeValue attributeValue=findAttributeValue(attribute);
        if(attributeValue==null){
            return null;
        }
        return attributeValue.getValue();
    }

    /**
     * Find the attribute in a dataelement for the given code
     * @param dataElement
     * @param code
     * @return
     */
    public AttributeValue findAttributeValuefromDataElementCode(String code,DataElement dataElement){
        //select * from Attribute join AttributeValue on Attribute.id = attributeValue.attributeId join DataElementAttributeValue on attributeValue.id=DataElementAttributeValue.attributeValueId where DataElementAttributeValue.dataElementId="vWgsPN1RPLl" and code="Order"
        for (AttributeValue attributeValue: dataElement.getAttributeValues()){
            if(attributeValue.getAttribute().getCode()==null) {
                throw new RuntimeException(String.format(PreferencesState.getInstance().getContext().getResources().getString(R.string.dialog_error_attribute_null), attributeValue.getAttributeId()));
            }
            if (attributeValue.getAttribute().getCode().equals(code))
                return attributeValue;
        }
        return null;
    }

    /**
     * Find the associated prgoramStage (tabgroup)
     *
     * @return
     */
    public String findProgramUID(){
        return DataElementExtended.findProgramUIDByDataElementUID(getDataElement().getUid());
    }

    /**
     * Find the associated program (tabgroup) given a dataelement UID
     *
     * @param dataElementUID
     * @return
     */
    public static String findProgramUIDByDataElementUID(String dataElementUID) {
        //Find the right 'uid' of the dataelement program
        Program program = new Select().from(Program.class).as("p")
                .join(ProgramStage.class, Join.JoinType.LEFT).as("ps")
                .on(Condition.column(ColumnAlias.columnWithTable("p", Program$Table.ID))
                        .eq(ColumnAlias.columnWithTable("ps", ProgramStage$Table.PROGRAM)))
                .join(ProgramStageDataElement.class, Join.JoinType.LEFT).as("psd")
                .on(Condition.column(ColumnAlias.columnWithTable("psd", ProgramStageDataElement$Table.PROGRAMSTAGE))
                        .eq(ColumnAlias.columnWithTable("ps", ProgramStage$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("psd", ProgramStageDataElement$Table.DATAELEMENT)).eq(dataElementUID))
                .querySingle();
        if (program == null) {
            return null;
        }
        return program.getUid();
    }

    /**
     * Find the associated programStageSection (tab)UID given a dataelement UID
     *
     * @param dataElementUID
     * @return
     */
    public static String findProgramStageSectionUIDByDataElementUID(String dataElementUID) {
        //Find the right 'uid' of the dataelement program
        ProgramStageSection programSS = new Select().from(ProgramStageSection.class).as("pss")
                .join(ProgramStageDataElement.class, Join.JoinType.LEFT).as("psde")
                .on(Condition.column(ColumnAlias.columnWithTable("pss", ProgramStageSection$Table.ID))
                        .eq(ColumnAlias.columnWithTable("psde", ProgramStageDataElement$Table.PROGRAMSTAGESECTION)))
                .where(Condition.column(ColumnAlias.columnWithTable("psde", ProgramStageDataElement$Table.DATAELEMENT)).eq(dataElementUID))
                .querySingle();
        if (programSS == null) {
            return null;
        }
        return programSS.getUid();
    }


    /**
     * Find the associated ProgramStageDataElement (tab) given a dataelement UID
     *
     * @param dataElementUID
     * @return
     */
    public static ProgramStageDataElement findProgramStageDataElementByDataElementUID(String dataElementUID) {
        //Find the right 'uid' of the dataelement program
        ProgramStageDataElement programDE = new Select().from(ProgramStageDataElement.class).as("psde")
                .where(Condition.column(ColumnAlias.columnWithTable("psde", ProgramStageDataElement$Table.DATAELEMENT)).eq(dataElementUID))
                .querySingle();
        if (programDE == null) {
            return null;
        }
        return programDE;
    }
    /**
     * Find the order from dataelement in programStage
     *
     * @param dataElementUID
     * @return
     */
    public static String findProgramStageSectionOrderDataElementOrderByDataElementUID(String dataElementUID) {
        //Find the right 'uid' of the dataelement program
        ProgramStageSection programSS = new Select().from(ProgramStageSection.class).as("pss")
                .join(ProgramStageDataElement.class, Join.JoinType.LEFT).as("psd")
                .on(Condition.column(ColumnAlias.columnWithTable("psd", ProgramStageDataElement$Table.PROGRAMSTAGESECTION))
                                .eq(ColumnAlias.columnWithTable("pss", ProgramStageSection$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("psd", ProgramStageDataElement$Table.DATAELEMENT)).eq(dataElementUID))
                .querySingle();
        if (programSS == null) {
            return null;
        }
        return programSS.getSortOrder()+"";
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
            float numinator = Float.valueOf(value);
            return numinator;
        } else
            return findDenominator();
    }

    public Float findDenominator() {
        String value = getValue(ATTRIBUTE_DENUMERATOR);
        if (value != null && !value.equals("")) {
            float denominator = Float.valueOf(value);
            return denominator;
        }
        return 0.0f;
    }

    public CompositeScore findCompositeScore() {
        CompositeScore compositeScore = null;

        String value = findCompositeScoreId();
        if (value != null) {
            try {
                compositeScore = CompositeScoreBuilder.getCompositeScoreFromDataElementAndHierarchicalCode(getDataElement(), value);
            } catch (Exception e) {
                return compositeScore;
            }
        }
        return compositeScore;
    }
}
