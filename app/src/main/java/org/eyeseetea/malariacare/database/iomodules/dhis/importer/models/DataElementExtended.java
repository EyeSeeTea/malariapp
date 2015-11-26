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

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.hisp.dhis.android.sdk.persistence.models.Attribute;
import org.hisp.dhis.android.sdk.persistence.models.Attribute$Table;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.DataElementAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.DataElementAttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.Program$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection$Table;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class DataElementExtended implements VisitableFromSDK {

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
     * Code of attribute of DETabName for header
     */
    public static final String ATTRIBUTE_TAB_NAME = "DETabName";
    /**
     * Code of attribute '20 Question Type'
     */
    public static final String ATTRIBUTE_QUESTION_TYPE_CODE = "DEQuesType";
    /**
     * Value to discard the COMPOSITE_SCORE
     */
    public static final String CONTROLDATAELEMENT_NAME = "CONTROL_DATAELEMENT";
    /**
     * Code of attribute 'Composite Score'
     */
    public static final String ATTRIBUTE_COMPOSITE_SCORE_CODE = "DECompositiveScore";
    /**
     * Value to discard the dataelementcontrol
     */
    public static final String COMPOSITE_SCORE_NAME = "COMPOSITE_SCORE";
    /**
     * Value parent
     */
    public static final String PARENT = "PARENT";
    /**
     * Value child
     */
    public static final String CHILD = "CHILD";

    /**
     * Code to identify control dataElements
     */
    private static String COMPOSITE_SCORE_CODE = "";
    /**
     * Code to identify composite scores
     */
    private static String CONTROL_DATAELEMENT_CODE = "";

    DataElement dataElement;

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

    public boolean isCompositeScore() {
        String typeQuestion = findAttributeValueByCode(ATTRIBUTE_QUESTION_TYPE_CODE);

        if (typeQuestion == null) {
            return false;
        }

        if (COMPOSITE_SCORE_CODE.equals("")) {
            COMPOSITE_SCORE_CODE = OptionExtended.findOptionByName(COMPOSITE_SCORE_NAME).getCode();
        }

        return typeQuestion.equals(COMPOSITE_SCORE_CODE);
    }

    public boolean isControlDataElement() {

        String typeQuestion = findAttributeValueByCode(ATTRIBUTE_QUESTION_TYPE_CODE);

        if (typeQuestion == null) {
            return false;
        }

        if (CONTROL_DATAELEMENT_CODE.equals("")) {
            CONTROL_DATAELEMENT_CODE = OptionExtended.findOptionByName(CONTROLDATAELEMENT_NAME).getCode();
        }

        return typeQuestion.equals(CONTROL_DATAELEMENT_CODE);
    }

    public boolean isQuestion() {
        if (isControlDataElement()) {
            return false;
        }
        return !isCompositeScore();
    }

    /**
     * Find the attributevalue in a dataelement for the given attribute
     * @param attribute
     * @return
     */
    public AttributeValue findAttributeValue(Attribute attribute){
        return new Select().from(AttributeValue.class).as("av")
                .join(DataElementAttributeValue.class, Join.JoinType.LEFT).as("dea")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("dea", DataElementAttributeValue$Table.ATTRIBUTEVALUE_ATTRIBUTEVALUEID))
                                .eq(ColumnAlias.columnWithTable("av", AttributeValue$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("dea", DataElementAttributeValue$Table.DATAELEMENTID)).eq(dataElement.getUid()))
                        //For the given survey
                .and(Condition.column(ColumnAlias.columnWithTable("av", AttributeValue$Table.ATTRIBUTE_ATTRIBUTEID)).eq(attribute.getUid())).querySingle();
    }


    /**
     * Finds the value of an attribute with the given code in a dataElement
     * @param code
     * @return
     */
    public  String findAttributeValueByCode(String code){

        //Find the right attribute
        Attribute attribute= AttributeExtended.findAttributeByCode(code);
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
     * Find the attribute in a dataelement for the given attribute
     * @param dataElement
     * @param code
     * @return
     */
    public AttributeValue findAttributeValuefromDataElementCode(String code,DataElement dataElement){
        //select * from Attribute join AttributeValue on Attribute.id = attributeValue.attributeId join DataElementAttributeValue on attributeValue.id=DataElementAttributeValue.attributeValueId where DataElementAttributeValue.dataElementId="vWgsPN1RPLl" and code="Order"
        return new Select().from(AttributeValue.class).as("av")
                .join(Attribute.class, Join.JoinType.LEFT).as("at")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("at", Attribute$Table.ID))
                                .eq(ColumnAlias.columnWithTable("av", AttributeValue$Table.ATTRIBUTE_ATTRIBUTEID)))
                .join(DataElementAttributeValue.class, Join.JoinType.LEFT).as("dea")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("dea", DataElementAttributeValue$Table.ATTRIBUTEVALUE_ATTRIBUTEVALUEID))
                                .eq(ColumnAlias.columnWithTable("av", AttributeValue$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("dea", DataElementAttributeValue$Table.DATAELEMENTID)).eq(dataElement.getUid()))
                        //For the given survey
                .and(Condition.column(ColumnAlias.columnWithTable("at", Attribute$Table.CODE)).eq(code)).querySingle();
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
     * Find the associated programStageSection (tab) given a dataelement UID
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
        if (value != null) {
            float numinator = Float.valueOf(value);
            return numinator;
        } else
            return 0.0f;
    }

    public Float findDenominator() {
        String value = getValue(ATTRIBUTE_DENUMERATOR);
        if (value != null) {
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
