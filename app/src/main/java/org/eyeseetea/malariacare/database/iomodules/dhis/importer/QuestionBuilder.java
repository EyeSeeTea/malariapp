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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer;

import android.util.Log;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Question;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ignac on 14/11/2015.
 */
public class QuestionBuilder {

    private static final String TAG = ".CompositeScoreBuilder";

    /**
     * Expected value for the attributeValue DeQuesType in those dataElements which are a CompositeScore
     */
    private static final String COMPOSITE_SCORE_NAME = "COMPOSITE_SCORE";
    /**
     * Value of option 'COMPOSITE_SCORE'
     */
    private static String COMPOSITE_SCORE_CODE;
    /**
     * Code of attribute Header name
     */
    private static final String ATTRIBUTE_HEADER_NAME = "DEHeader";
    /**
     * Code of attribute order int
     */
    private static final String ATTRIBUTE_ORDER = "Order";
    /**
     * Code of attribute numerator float
     */
    private static final String ATTRIBUTE_NUMERATOR = "DENumerator";
    /**
     * Code of attribute denominator float
     */
    private static final String ATTRIBUTE_DENUMERATOR = "DEDenominator";
    /**
     * Code of attribute of group of patern/child relation
     */
    private static final String ATTRIBUTE_HIDE_GROUP = "DEHideGroup";
    /**
     * Code of attribute of type patern or child
     */
    private static final String ATTRIBUTE_HIDE_TYPE = "DEHideType";
    /**
     * Code of attribute of Match group of patern/child relation
     */
    private static final String ATTRIBUTE_MATCH_GROUP = "DEMatchGroup";
    /**
     * Code of attribute of Match type patern or child
     */
    private static final String ATTRIBUTE_MATCH_TYPE = "DEMatchType";
    /**
     * Code of attribute of DETabName for header
     */
    private static final String ATTRIBUTE_TAB_NAME = "DETabName";
    /**
     * Value parent
     */
    private static final String PARENT = "PARENT";
    /**
     * Value child
     */
    private static final String CHILD = "CHILD";

    /**
     * Code of attribute '35 Composite Score'
     */
    private static final String ATTRIBUTE_COMPOSITE_SCORE_CODE = "DECompositiveScore";

    /**
     *
     */
    Map<String, Question> mapQuestions;

    /**
     *
     */
    Map<String, String> mapParent;
    /**
     *
     */
    Map<String, String> mapType;
    /**
     *
     */
    Map<String, String> mapLevel;
    /**
     *
     */
    Map<String, String> mapMatchType;
    /**
     *
     */
    Map<String, String> mapMatchLevel;
    /**
     *
     */
    Map<String, String> mapMatchParent;
    /**
     *
     */
    Map<String, Header> mapHeader;
    /**
     * Helper required to deal with AttributeValues
     */
    AttributeValueHelper attributeValueHelper;


    private int header_order = 0;

    QuestionBuilder() {
        mapQuestions = new HashMap<>();
        mapHeader = new HashMap<>();
        mapType = new HashMap<>();
        mapLevel = new HashMap<>();
        mapParent = new HashMap<>();
        mapMatchLevel = new HashMap<>();
        mapMatchParent = new HashMap<>();
        mapMatchType = new HashMap<>();
        attributeValueHelper = new AttributeValueHelper();
    }

    /**
     * Registers a Question in builder
     *
     * @param question
     */
    public void add(Question question) {
        mapQuestions.put(question.getUid(), question);
    }

    public Integer findOrder(DataElement dataElement) {
        String value = attributeValueHelper.findAttributeValuefromDataElementCode(ATTRIBUTE_ORDER, dataElement).getValue();
        int order = Integer.valueOf(value);
        return order;
    }

    public Float findNumerator(DataElement dataElement) {
        String value = attributeValueHelper.findAttributeValuefromDataElementCode(ATTRIBUTE_NUMERATOR, dataElement).getValue();
        float numinator = Float.valueOf(value);
        return numinator;
    }

    public Float findDenominator(DataElement dataElement) {
        String value = attributeValueHelper.findAttributeValuefromDataElementCode(ATTRIBUTE_DENUMERATOR, dataElement).getValue();
        float denominator = Float.valueOf(value);
        return denominator;
    }

    public CompositeScore findCompositeScore(DataElement dataElement) {
        String value = attributeValueHelper.findAttributeValuefromDataElementCode(ATTRIBUTE_COMPOSITE_SCORE_CODE, dataElement).getValue();
        CompositeScore compositeScore = CompositeScoreBuilder.mapCompositeScores.get(value);
        if (compositeScore == null && value != null)
            Log.d("composite sin poner", value);
        return compositeScore;
        //return compositeScore;
    }

    public Header findHeader(DataElement dataElement) {
        Header header = null;
        String value = attributeValueHelper.findAttributeValuefromDataElementCode(ATTRIBUTE_HEADER_NAME, dataElement).getValue();
        if (!mapHeader.containsKey(value)) {
            header = new Header();
            header.setName(value.trim());
            header.setShort_name(value);
            value = attributeValueHelper.findAttributeValueByCode(ATTRIBUTE_TAB_NAME, dataElement);
            org.eyeseetea.malariacare.database.model.Tab questionTab = new org.eyeseetea.malariacare.database.model.Tab();
            questionTab = (org.eyeseetea.malariacare.database.model.Tab) ConvertFromSDKVisitor.appMapObjects.get(questionTab.getClass() + value);
            header.setOrder_pos(header_order);
            header_order++;
            header.setTab(questionTab);
            header.save();
            mapHeader.put(header.getName(), header);
        } else
            header = mapHeader.get(value);
        return header;
    }
    /**
     * Registers a Parent/child relations in map
     *
     * @param dataElement
     */
    public void findParent(DataElement dataElement) {
        String questionRelationType=null;
        String questionRelationGroup=null;
        String matchRelationType=null;
        String matchRelationGroup=null;
        questionRelationType =getValue(ATTRIBUTE_HIDE_TYPE,dataElement);
        questionRelationGroup =getValue(ATTRIBUTE_HIDE_GROUP, dataElement);
        matchRelationType =getValue(ATTRIBUTE_MATCH_TYPE, dataElement);
        matchRelationGroup =getValue(ATTRIBUTE_MATCH_GROUP, dataElement);
        Question questionParent = null;
        if (questionRelationType != null) {
            if (questionRelationType.equals(PARENT)) {
                mapParent.put(questionRelationGroup, dataElement.getUid());
            }
            mapType.put(dataElement.getUid(), questionRelationType);
            mapLevel.put(dataElement.getUid(), questionRelationGroup);
        }
        else if(questionRelationGroup!=null){
            mapParent.put(questionRelationGroup, dataElement.getUid());
        }
        if (matchRelationType != null) {
            if (matchRelationType.equals(PARENT)) {
                mapMatchParent.put(matchRelationGroup, dataElement.getUid());
            }
            mapMatchType.put(dataElement.getUid(), matchRelationType);
            mapMatchLevel.put(dataElement.getUid(), matchRelationGroup);
        }
    }

    /**
     * Save Question id_parent QuestionOption QuestionRelation and Match
     *
     * @param dataElement
     */
    public void addRelations(DataElement dataElement) {
            addParent(dataElement);
            addQuestionRelations(dataElement);
    }

    private void addQuestionRelations(DataElement dataElement) {
        String matchRelationType = mapMatchType.get(dataElement.getUid());
        String matchRelationGroup = mapMatchLevel.get(dataElement.getUid());

        org.eyeseetea.malariacare.database.model.Question appQuestion = (org.eyeseetea.malariacare.database.model.Question) mapQuestions.get(dataElement.getUid());

        if (matchRelationType != null && matchRelationType.equals(PARENT)) {

        } else if (matchRelationType != null && matchRelationType.equals(CHILD)) {
            try {
                org.eyeseetea.malariacare.database.model.QuestionRelation questionRelation = new org.eyeseetea.malariacare.database.model.QuestionRelation();
                org.eyeseetea.malariacare.database.model.Match match = new org.eyeseetea.malariacare.database.model.Match();

                if (matchRelationType.equals(CHILD)) {
                    questionRelation.setOperation(1);
                    questionRelation.setQuestion(appQuestion);
                    questionRelation.save();

                    String parentuid = mapMatchParent.get(matchRelationGroup);
                    if (parentuid != null) {
                        org.eyeseetea.malariacare.database.model.Question parentQuestion = (org.eyeseetea.malariacare.database.model.Question) mapQuestions.get(parentuid);
                        List<org.eyeseetea.malariacare.database.model.Option> options = parentQuestion.getAnswer().getOptions();
                        for (org.eyeseetea.malariacare.database.model.Option option : options) {
                            org.eyeseetea.malariacare.database.model.QuestionOption questionOption = new org.eyeseetea.malariacare.database.model.QuestionOption();

                            questionOption.setOption(option);
                            questionOption.setQuestion(parentQuestion);
                            //fixme
                            //questionOption.setOption();
                            match.setQuestionRelation(questionRelation);
                            match.save();
                            questionOption.setMatch(match);
                            questionOption.save();
                        }
                    }
                }
            } catch (Exception e) {
                Log.d("MyExcepcion","addRelationsLevel2");
            }
        } else {
            org.eyeseetea.malariacare.database.model.QuestionRelation questionRelation = new org.eyeseetea.malariacare.database.model.QuestionRelation();
            questionRelation.setOperation(0);
            questionRelation.setQuestion(appQuestion);
            questionRelation.save();
        }
    }

    private void addParent(DataElement dataElement) {
        String questionRelationType = mapType.get(dataElement.getUid());
        String questionRelationGroup = mapLevel.get(dataElement.getUid());
        if (questionRelationType!=null && questionRelationType.equals(CHILD)) {
            String uid = mapParent.get(questionRelationGroup);
            Question parent = mapQuestions.get(uid);
            Question child = mapQuestions.get(dataElement.getUid());
            child.setQuestion(parent);
            child.save();
        }
    }

    /**
     * Gets value if the AttributeValue is not null
     *
     * @param attributeCode
     * @param dataElement
     * @return value
     */
    private String getValue(String attributeCode, DataElement dataElement) {
        AttributeValue attributeValue;
        attributeValue =attributeValueHelper.findAttributeValuefromDataElementCode(attributeCode, dataElement);
        if(attributeValue!=null) {
            return attributeValue.getValue();
        }
        return null;
    }

}
