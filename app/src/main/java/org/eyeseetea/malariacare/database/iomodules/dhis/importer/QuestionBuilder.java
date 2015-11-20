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

import android.provider.ContactsContract;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionExtended;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header; ;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage$Table;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.Program$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection$Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ignac on 14/11/2015.
 */
public class QuestionBuilder {

    private static final String TAG=".QuestionBuilder";
    /**
     * Code of attribute dheader unique name
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
     * Code of attribute '20 Question Type'
     */
    private static final String ATTRIBUTE_QUESTION_TYPE_CODE = "DEQuesType";
    /**
     * Value parent
     */
    private static final String PARENT = "PARENT";
    /**
     * Value child
     */
    private static final String CHILD = "CHILD";

    /**
     * Code of attribute 'Composite Score'
     */
    private static final String ATTRIBUTE_COMPOSITE_SCORE_CODE = "DECompositiveScore";

    //Fixme In the future, when isAQuestion check if is a question, it should be removed
    /**
     * Value to discard the dataelementcontrol
     */
    private static final String COMPOSITE_SCORE_NAME = "COMPOSITE_SCORE";
    /**
     * Code to discard the dataelementcontrol
     */
    private static String COMPOSITE_SCORE_CODE = "";
    /**
     * Value to discard the COMPOSITE_SCORE
     */
    private static final String DATAELEMENTCONTROL_NAME = "CONTROL_DATAELEMENT";
    /**
     * Code to discard the COMPOSITE_SCORE
     */
    private static String DATAELEMENTCONTROL_CODE = "";

    /**
     * Mapping all the questions
     */
    Map<String, Question> mapQuestions;

    /**
     * Mapping all the question parents
     */
    Map<String, String> mapParent;
    /**
     * Mapping all the question type(child/parent)
     */
    Map<String, String> mapType;
    /**
     * Mapping all the question level(it is needed for know who are the parent)
     */
    Map<String, String> mapLevel;
    /**
     * Mapping all the Match question parents
     */
    Map<String, String> mapMatchType;
    /**
     * Mapping all the Match question type(child/parent)
     */
    Map<String, String> mapMatchLevel;
    /**
     * Mapping all the Match question level(it is needed for know who are the parent)
     */
    Map<String, String> mapMatchParent;
    /**
     * Mapping all the Match question level(it is needed for know who are the parent)
     */
    Map<String, List<String>> mapMatchChilds;
    /**
     * Mapping headers(it is needed for not duplicate data)
     */
    Map<String, Header> mapHeader;

    /**
     * It is needed in the header order.
     */
    private int header_order = 0;

    QuestionBuilder() {
        mapQuestions = new HashMap<>();
        mapHeader = new HashMap<>();
        mapType = new HashMap<>();
        mapLevel = new HashMap<>();
        mapParent = new HashMap<>();
        mapMatchLevel = new HashMap<>();
        mapMatchParent = new HashMap<>();
        mapMatchChilds = new HashMap<>();
        mapMatchType = new HashMap<>();

        //Fixme In the future, when isAQuestion check if is a question, it should be removed
        Option optionDataElementControl = OptionExtended.findOptionByName(DATAELEMENTCONTROL_NAME);
        DATAELEMENTCONTROL_CODE = optionDataElementControl.getCode();
        Option optionCompositeScore = OptionExtended.findOptionByName(COMPOSITE_SCORE_NAME);
        COMPOSITE_SCORE_CODE = optionCompositeScore.getCode();
    }

    /**
     * Registers a Question in builder
     *
     * @param question
     */
    public void add(Question question) {
        mapQuestions.put(question.getUid(), question);
    }

    public Integer findOrder(DataElementExtended dataElementExtended) {
        String value = getValue(ATTRIBUTE_ORDER, dataElementExtended);
        if (value != null) {
            int order = Integer.valueOf(value);
            return order;
        }
        return null;
    }

    public String findCompositeScoreId(DataElementExtended dataElementExtended) {
        String value = getValue(ATTRIBUTE_COMPOSITE_SCORE_CODE, dataElementExtended);
        return value;
    }

    public Float findNumerator(DataElementExtended dataElementExtended) {
        String value = getValue(ATTRIBUTE_NUMERATOR, dataElementExtended);
        if (value != null) {
            float numinator = Float.valueOf(value);
            return numinator;
        } else
            return null;
    }

    public Float findDenominator(DataElementExtended dataElementExtended) {
        String value = getValue(ATTRIBUTE_DENUMERATOR, dataElementExtended);
        if (value != null) {
            float denominator = Float.valueOf(value);
            return denominator;
        }
        return null;
    }

    /**
     * Return a CompositeScore question
     * <p/>
     * The compositeScore is getted in mapCompositeScores in CompositeScoreBuilder.class
     *
     * @param dataElementExtended
     * @return compositeScore question
     */
    public CompositeScore findCompositeScore(DataElementExtended dataElementExtended) {
        CompositeScore compositeScore = null;

        String value = findCompositeScoreId(dataElementExtended);
        if (value != null) {
            try {
                compositeScore = CompositeScoreBuilder.getCompositeScoreFromDataElementAndHierarchicalCode(dataElementExtended.getDataElement(), value);
            } catch (Exception e) {
                return compositeScore;
            }
        }
        return compositeScore;
    }

    /**
     * Save and return Header question
     * <p/>
     * The header check if exist before be saved.
     *
     * @param dataElementExtended
     * @return header question
     */
    public Header findHeader(DataElementExtended dataElementExtended) {
        Header header = null;
        String value = getValue(ATTRIBUTE_HEADER_NAME, dataElementExtended);
        if (value != null) {
            if (!mapHeader.containsKey(value)) {
                header = new Header();
                header.setName(value.trim());
                header.setShort_name(value);
                value = getValue(ATTRIBUTE_TAB_NAME, dataElementExtended);
                org.eyeseetea.malariacare.database.model.Tab questionTab = new org.eyeseetea.malariacare.database.model.Tab();
                questionTab = (org.eyeseetea.malariacare.database.model.Tab) ConvertFromSDKVisitor.appMapObjects.get(questionTab.getClass() + value);
                header.setOrder_pos(header_order);
                header_order++;
                header.setTab(questionTab);
                header.save();
                mapHeader.put(header.getName(), header);
            } else
                header = mapHeader.get(value);
        }
        return header;
    }

    /**
     * Registers a Parent/child and Match Parent/child relations in maps
     *
     * @param dataElementExtended
     */
    public void RegisterParentChildRelations(DataElementExtended dataElementExtended) {
        DataElement dataElement = dataElementExtended.getDataElement();
        String questionRelationType = null;
        String questionRelationGroup = null;
        String matchRelationType = null;
        String matchRelationGroup = null;
        questionRelationType = getValue(ATTRIBUTE_HIDE_TYPE, dataElementExtended);
        questionRelationGroup = getValue(ATTRIBUTE_HIDE_GROUP, dataElementExtended);
        matchRelationType = getValue(ATTRIBUTE_MATCH_TYPE, dataElementExtended);
        matchRelationGroup = getValue(ATTRIBUTE_MATCH_GROUP, dataElementExtended);
        if (questionRelationType != null) {
            String parentProgramUid=findProgramUIDByDataElementUID(dataElement.getUid());
            if (questionRelationType.equals(PARENT)) {
                mapParent.put(parentProgramUid+questionRelationGroup, dataElement.getUid());
            }
            else {
                mapType.put(parentProgramUid + dataElement.getUid(), questionRelationType);
                mapLevel.put(parentProgramUid + dataElement.getUid(), questionRelationGroup);
            }
    }
        if (matchRelationType != null) {
            String parentProgramUid=findProgramUIDByDataElementUID(dataElement.getUid());
            if (matchRelationType.equals(PARENT)) {
                mapMatchParent.put(parentProgramUid+matchRelationGroup, dataElement.getUid());
            } else if (matchRelationType.equals(CHILD)) {
                List <String> childsUids;
                if(mapMatchChilds.containsKey(parentProgramUid+matchRelationGroup)){
                    childsUids=mapMatchChilds.get(parentProgramUid+matchRelationGroup);
                }
                else{
                    childsUids = new ArrayList<>();
                }
                childsUids.add(dataElement.getUid());
                mapMatchChilds.put(parentProgramUid+matchRelationGroup, childsUids);
            }
            mapMatchType.put(parentProgramUid + dataElement.getUid(), matchRelationType);
            mapMatchLevel.put(parentProgramUid+dataElement.getUid(), matchRelationGroup);

        }

    }

    /**
     * Save Question id_parent QuestionOption QuestionRelation and Match
     *
     * @param dataElementExtended
     */
    public void addRelations(DataElementExtended dataElementExtended) {
        if (mapQuestions.containsKey(dataElementExtended.getDataElement().getUid())) {
            addParent(dataElementExtended.getDataElement());
            addQuestionRelations(dataElementExtended.getDataElement());
            addCompositeScores(dataElementExtended);
        }
    }

    private void addCompositeScores(DataElementExtended dataElementExtended) {
        CompositeScore compositeScore = findCompositeScore(dataElementExtended);
        if (compositeScore != null) {
            org.eyeseetea.malariacare.database.model.Question appQuestion = (org.eyeseetea.malariacare.database.model.Question) mapQuestions.get(dataElementExtended.getDataElement().getUid());
            if (appQuestion != null) {
                appQuestion.setCompositeScore(compositeScore);
                appQuestion.save();
                add(appQuestion);
            }
        }
    }

    /**
     * Create QuestionOption QuestionRelation and Match relations
     *
     * checks if the dataElement is a parent(if is a parent it have mapMatchType and mapMatchLevel)
     * Later get the two childs and create the relation
     * it needs check what Options factors do match, and check it with method getMatchOption() .
     * @param dataElement
     */
    public static boolean debug=false;
    private void addQuestionRelations(DataElement dataElement) {

        String programUid=findProgramUIDByDataElementUID(dataElement.getUid());
        String matchRelationType = mapMatchType.get(programUid+dataElement.getUid());
        String matchRelationGroup = mapMatchLevel.get(programUid+dataElement.getUid());
        if(debug==false) {
            Log.d(TAG, "MatchTypes:" + mapMatchType.size() + " MatchLevels" + mapMatchLevel.size() + " MatchParents " + mapMatchParent.size() + " MapChildrens" + mapMatchChilds);
            debug=true;
        }
        org.eyeseetea.malariacare.database.model.Question appQuestion = (org.eyeseetea.malariacare.database.model.Question) mapQuestions.get(dataElement.getUid());

        if (matchRelationType != null && matchRelationType.equals(PARENT)) {
            try {

                if(debug==false)
                Log.d(TAG,"Parent encontrado");
                org.eyeseetea.malariacare.database.model.QuestionRelation questionRelation = new org.eyeseetea.malariacare.database.model.QuestionRelation();
                org.eyeseetea.malariacare.database.model.Match match = new org.eyeseetea.malariacare.database.model.Match();
                    questionRelation.setOperation(0);
                Log.d(TAG, "QuestionRelation: 0-" + appQuestion.getUid());
                    questionRelation.setQuestion(appQuestion);
                    questionRelation.save();

                List<String> mapChilds = mapMatchChilds.get(programUid+matchRelationGroup);
                Question child[] = new Question[2];
                if(debug==false)
                Log.d(TAG, "Mapchilds size: " + mapChilds.size());
                    int count = 0;
                    for (String uid:mapChilds) {
                        child[count] = mapQuestions.get(uid);
                        count++;
                    }
                    try {
                        ArrayList<Float>  optionCode = getMatchOption(child[0], child[1]);
                        Log.d(TAG, "Factor: " + optionCode);

                        for (int i = 0; i < child.length; i++) {
                            List<org.eyeseetea.malariacare.database.model.Option> options = child[i].getAnswer().getOptions();
                            for (org.eyeseetea.malariacare.database.model.Option option : options) {
                                if(debug==false)
                                Log.d(TAG, "compare: factor-" +optionCode+ " option " +option.getFactor());
                                if (optionCode.contains(option.getFactor())) {
                                    org.eyeseetea.malariacare.database.model.QuestionOption questionOption = new org.eyeseetea.malariacare.database.model.QuestionOption();
                                    questionOption.setOption(option);
                                    questionOption.setQuestion(child[i]);
                                    if(debug==false)
                                    Log.d(TAG, "QuestionOption: option" + option.getName() + " question " + child[i].getUid());
                                    match.setQuestionRelation(questionRelation);
                                    if(debug==false)
                                    Log.d(TAG, "QuestionRelation" + questionRelation.getId_question_relation());
                                    match.save();

                                    questionOption.setMatch(match);
                                    if(debug==false)
                                    Log.d(TAG, "Match:" + questionOption.getId_question_option());
                                    questionOption.save();
                                    Log.d(TAG, "All saved");
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Error not saved");
                        e.printStackTrace();
                    }
            } catch (Exception e) {
                Log.d(TAG, "Error not saved");
                e.printStackTrace();
            }
        }
    }

    private ArrayList<Float>  getMatchOption(Question question, Question question2) {
        ArrayList<Float> optionFactors= new ArrayList<>();
        Log.d(TAG,question.getUid());
        Log.d(TAG,question2.getUid());

        List<org.eyeseetea.malariacare.database.model.Option> options = question.getAnswer().getOptions();
        List<org.eyeseetea.malariacare.database.model.Option> options2 = question2.getAnswer().getOptions();
        for (org.eyeseetea.malariacare.database.model.Option option : options) {
            for (org.eyeseetea.malariacare.database.model.Option option2 : options2) {
                if (option.getFactor().equals(option2.getFactor())) {
                    if(!optionFactors.contains(option2.getFactor()))
                    optionFactors.add(option.getFactor());
                }
            }
        }
        return optionFactors;
    }

    /**
     * Save Question id_parent in Question
     *
     * @param dataElement
     */
    private void addParent(DataElement dataElement) {
        String programUid=findProgramUIDByDataElementUID(dataElement.getUid());
        String questionRelationType = mapType.get(programUid+dataElement.getUid());
        String questionRelationGroup = mapLevel.get(programUid+dataElement.getUid());

        org.eyeseetea.malariacare.database.model.Question appQuestion = (org.eyeseetea.malariacare.database.model.Question) mapQuestions.get(dataElement.getUid());

        if (questionRelationType != null && questionRelationType.equals(CHILD)) {
            try {

                org.eyeseetea.malariacare.database.model.QuestionRelation questionRelation = new org.eyeseetea.malariacare.database.model.QuestionRelation();
                org.eyeseetea.malariacare.database.model.Match match = new org.eyeseetea.malariacare.database.model.Match();

                if (questionRelationType.equals(CHILD)) {
                    questionRelation.setOperation(1);
                    questionRelation.setQuestion(appQuestion);
                    questionRelation.save();
                    String parentuid = mapParent.get(programUid+questionRelationGroup);
                    if (parentuid != null) {
                        org.eyeseetea.malariacare.database.model.Question parentQuestion = (org.eyeseetea.malariacare.database.model.Question) mapQuestions.get(parentuid);
                        List<org.eyeseetea.malariacare.database.model.Option> options = parentQuestion.getAnswer().getOptions();
                        for (org.eyeseetea.malariacare.database.model.Option option : options) {
                            if (option.getName().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.yes))) {
                                org.eyeseetea.malariacare.database.model.QuestionOption questionOption = new org.eyeseetea.malariacare.database.model.QuestionOption();
                                questionOption.setOption(option);
                                questionOption.setQuestion(parentQuestion);

                                match.setQuestionRelation(questionRelation);
                                match.save();

                                questionOption.setMatch(match);
                                questionOption.save();
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Gets value if the AttributeValue is not null
     *
     * @param attributeCode
     * @param dataElementExtended
     * @return value
     */
    private String getValue(String attributeCode, DataElementExtended dataElementExtended) {
        AttributeValue attributeValue;
        DataElement dataElement = dataElementExtended.getDataElement();
        attributeValue = dataElementExtended.findAttributeValuefromDataElementCode(attributeCode, dataElement);
        if (attributeValue != null) {
            return attributeValue.getValue();
        }
        return null;
    }


    //Fixme isAQuestion is necessary to check the question when dataElements have a specific identifier for questions. Now it is detected by elimination
    public boolean isAQuestion(DataElementExtended dataElementExtended) {
        if (isDataElementControl(dataElementExtended)) {
            return false;
        }
        if (isCompositeScore(dataElementExtended)) {
            return false;
        }
        return true;
    }

    //Fixme In the future it should be removed
    private boolean isCompositeScore(DataElementExtended dataElementExtended) {
        String typeQuestion = dataElementExtended.findAttributeValueByCode(ATTRIBUTE_QUESTION_TYPE_CODE);

        if (typeQuestion == null) {
            return false;
        }

        return typeQuestion.equals(COMPOSITE_SCORE_CODE);
    }

    //Fixme In the future it should be removed
    public boolean isDataElementControl(DataElementExtended dataElementExtended) {

        String typeQuestion = dataElementExtended.findAttributeValueByCode(ATTRIBUTE_QUESTION_TYPE_CODE);

        if (typeQuestion == null) {
            return false;
        }

        return typeQuestion.equals(DATAELEMENTCONTROL_CODE);
    }


    /**
     * Find the associated prgoramStage (tabgroup) given a dataelement UID
     * @param dataElementUID
     * @return
     */
    private static String findProgramStageByDataElementUID(String dataElementUID){
        //Find the right 'tabgroup' to group scores by program
        ProgramStageDataElement programStageDataElement = new Select().from(ProgramStageDataElement.class)
                .where(Condition.column(ProgramStageDataElement$Table.DATAELEMENT)
                        .is(dataElementUID)).querySingle();

        if(programStageDataElement==null){
            return null;
        }

        return programStageDataElement.getProgramStage();
    }
    /**
     * Find the associated prgoramStage (tabgroup) given a dataelement UID
     * @param dataElementUID
     * @return
     */
    private static String findProgramUIDByDataElementUID(String dataElementUID){
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
        if(program==null){
            return null;
        }
        return program.getUid();
    }
}
