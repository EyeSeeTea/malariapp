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

import android.support.annotation.NonNull;
import android.util.Log;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Media;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramTabDict;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ignac on 14/11/2015.
 */
public class QuestionBuilder {

    private static final String TAG = ".QuestionBuilder";

    /**
     * Maps relationship between type of media and DEAttribute code
     */
    private static final Map<Integer, String> MAP_MEDIATYPE_DEATTRIBUTE;
    static {
        MAP_MEDIATYPE_DEATTRIBUTE = new HashMap<>();
        MAP_MEDIATYPE_DEATTRIBUTE.put(Constants.MEDIA_TYPE_IMAGE, DataElementExtended.ATTRIBUTE_IMAGE);
        MAP_MEDIATYPE_DEATTRIBUTE.put(Constants.MEDIA_TYPE_VIDEO, DataElementExtended.ATTRIBUTE_VIDEO);
    }

    /**
     * It is the factor needed in a option to create the questionRelation
     * */
    private final float MATCHFACTOR=1f;
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
     * Mapping headers(it is needed for not duplicate data)
     */
    Map<String, Program> mapProgram;

    /**
     * Contains all media (required for batch save)
     */
    List<Media> listMedia;
    /**
     * It is needed in the header order.
     */
    private int header_order = 0;


    /**
     * It is needed by child/parent relationships
     */
    private final String OPTIONSUBTOKEN=",";
    private final String PARENTTOKEN=",";
    private final String OPTIONTOKEN=";";
    QuestionBuilder() {
        mapQuestions = new HashMap<>();
        mapHeader = new HashMap<>();
        mapProgram = new HashMap<>();
        mapType = new HashMap<>();
        mapLevel = new HashMap<>();
        mapParent = new HashMap<>();
        mapMatchLevel = new HashMap<>();
        mapMatchParent = new HashMap<>();
        mapMatchChilds = new HashMap<>();
        mapMatchType = new HashMap<>();
        listMedia = new ArrayList<>();
    }

    public List<Media> getListMedia(){
        return this.listMedia;
    }

    /**
     * Registers a Question in builder
     *
     * @param question
     */
    public void add(Question question, String programUid) {
        mapQuestions.put(question.getUid()+programUid, question);
    }

    /**
     * Save and return the Header the question represented by the given DataElement belongs to
     *
     * @param dataElementExtended
     * @return question header
     */
    public Header findOrSaveHeader(DataElementExtended dataElementExtended, ProgramTabDict programTabDict, String programUid) {
        Header header;
        String attributeHeaderName = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_HEADER_NAME);
        //No header attribute no header
        if(attributeHeaderName==null){
            return null;
        }

        //Find tabUID
        String tabUid = dataElementExtended.findProgramStageSection();
        //No tab no header
        if(tabUid==null){
            return null;
        }
        //Unique key to index header
        String keyHeader=tabUid+attributeHeaderName;
        header = mapHeader.get(keyHeader);
        //Already built -> return
        if(header!=null){
            return header;
        }

        Tab tab = programTabDict.get(programUid, tabUid);
        //No tab-> something wrong
        if(tab==null){
            return null;
        }

        //First time
        header = buildHeader(attributeHeaderName, tab, tabUid);
        return header;
    }

    @NonNull
    private Header buildHeader(String attributeHeaderValue, Tab tab, String tabUID) {
        String keyHeader=tabUID+attributeHeaderValue;
        Header header;
        header = new Header();
        header.setName(attributeHeaderValue);
        header.setShort_name(attributeHeaderValue);
        header.setOrder_pos(header_order);
        header_order++;
        header.setTab(tab);
        header.save();

        mapHeader.put(keyHeader, header);
        return header;
    }

    /**
     * Create a Media object in the DB for each media attribute found  for a given question DE
     * @param dataElementExtended
     * @param question
     * @return
     */
    public void attachMedia(DataElementExtended dataElementExtended, Question question){
        // Loop on every media type to attach any possible media type for the DE
        for (Integer mediaType: MAP_MEDIATYPE_DEATTRIBUTE.keySet()) {
            String attributeMediaValue = dataElementExtended.getValue(MAP_MEDIATYPE_DEATTRIBUTE.get(mediaType));
            if (attributeMediaValue == null || attributeMediaValue.isEmpty()) {
                continue;
            }

            List<String> mediaReferences = Arrays.asList(attributeMediaValue.split(Constants.MEDIA_SEPARATOR));
            for(String mediaReference: mediaReferences){
                Log.i(TAG,String.format("Adding media %s to question %s",mediaReference, question.getForm_name()));
                Media media = new Media();
                media.setMediaType(mediaType);
                media.setResourceUrl(mediaReference);
                media.setQuestion(question);
                //media is saved in batch once questions are saved
                listMedia.add(media);
            }
        }
    }

    /**
     * Registers a Parent/child and Match Parent/child relations in maps
     * Its need the programid to diferenciate between programs dataelements.
     *
     * @param dataElementExtended
     */
    public void registerParentChildRelations(DataElementExtended dataElementExtended) {
        DataElement dataElement = dataElementExtended.getDataElement();
        String pogramUid = dataElementExtended.getProgramUid();
        String questionRelationType = null;
        String questionRelationGroup = null;
        String matchRelationType = null;
        String matchRelationGroup = null;
        questionRelationType = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_HIDE_TYPE);
        questionRelationGroup = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_HIDE_GROUP);
        matchRelationType = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_MATCH_TYPE);
        matchRelationGroup = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_MATCH_GROUP);
        if (questionRelationType != null) {
            if (questionRelationType.equals(DataElementExtended.PARENT)) {
                mapParent.put(pogramUid + questionRelationGroup, dataElement.getUid());
            } else {
                mapType.put(pogramUid + dataElement.getUid(), questionRelationType);
                mapLevel.put(pogramUid + dataElement.getUid(), questionRelationGroup);
            }
        }
        if (matchRelationType != null) {
            if (matchRelationType.equals(DataElementExtended.PARENT)) {
                mapMatchParent.put(pogramUid + matchRelationGroup, dataElement.getUid());
            } else if (matchRelationType.equals(DataElementExtended.CHILD)) {
                List<String> childsUids;
                if (mapMatchChilds.containsKey(pogramUid + matchRelationGroup)) {
                    childsUids = mapMatchChilds.get(pogramUid + matchRelationGroup);
                } else {
                    childsUids = new ArrayList<>();
                }
                childsUids.add(dataElement.getUid());
                mapMatchChilds.put(pogramUid + matchRelationGroup, childsUids);
            }
            mapMatchType.put(pogramUid + dataElement.getUid(), matchRelationType);
            mapMatchLevel.put(pogramUid + dataElement.getUid(), matchRelationGroup);

        }

    }

    /**
     * Save Question id_parent QuestionOption QuestionRelation and Match
     *
     * @param dataElementExtended
     */
    public void addRelations(DataElementExtended dataElementExtended) {
        if (mapQuestions.containsKey(dataElementExtended.getDataElement().getUid()+dataElementExtended.getProgramUid())) {
            addParent(dataElementExtended);
            registerMultiLevelParentChildRelation(dataElementExtended);
            addQuestionRelations(dataElementExtended);
            addCompositeScores(dataElementExtended);
        }
    }

    private void addCompositeScores(DataElementExtended dataElementExtended) {
        CompositeScore compositeScore = dataElementExtended.findCompositeScore();
        if (compositeScore != null) {
            Question appQuestion = mapQuestions.get(dataElementExtended.getDataElement().getUid()+dataElementExtended.getProgramUid());
            if (appQuestion != null) {
                appQuestion.setCompositeScore(compositeScore);
                appQuestion.save();
                add(appQuestion, dataElementExtended.getProgramUid());
            }
        }
    }

    /**
     * Save Question id_parent in Question
     *
     * @param dataElementExtended
     */
    private void addParent(DataElementExtended dataElementExtended) {
        String programUid = dataElementExtended.getProgramUid();
        DataElement dataElement= dataElementExtended.getDataElement();
        String questionRelationType = mapType.get(programUid + dataElement.getUid());
        String questionRelationGroup = mapLevel.get(programUid + dataElement.getUid());

        Question appQuestion = mapQuestions.get(dataElement.getUid()+programUid);

        if (questionRelationType != null && questionRelationType.equals(DataElementExtended.CHILD)) {
            try {
                if (questionRelationType.equals(DataElementExtended.CHILD)) {
                    String parentuid = mapParent.get(programUid + questionRelationGroup);
                    if (parentuid != null) {
                        QuestionRelation questionRelation = new QuestionRelation();
                        questionRelation.setOperation(1);
                        questionRelation.setQuestion(appQuestion);
                        boolean isSaved=false;
                        Question parentQuestion = mapQuestions.get(parentuid+programUid);
                        List<Option> options = parentQuestion.getAnswer().getOptions();
                        for (Option option : options)
                        {
                            if (option.getFactor()==MATCHFACTOR) {
                                if(!isSaved) {
                                    questionRelation.save();
                                    isSaved=true;
                                }
                                Match match = new Match();
                                match.setQuestionRelation(questionRelation);
                                match.save();
                                new QuestionOption(option, parentQuestion, match).save();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void registerMultiLevelParentChildRelation(DataElementExtended dataElementExtended){
        DataElement dataElement = dataElementExtended.getDataElement();
        String parentUids = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_PARENT_QUESTION);
        String optionsUids = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_PARENT_QUESTION_OPTIONS);
        if (parentUids==null || parentUids.equals("") || optionsUids==null || optionsUids.equals("")) {
            return;
        }
        int parentChildRelations = parentUids.length() - parentUids.replace(PARENTTOKEN, "").length();
        if(parentChildRelations!=optionsUids.length() - optionsUids.replace(OPTIONTOKEN, "").length()){
            Log.d(TAG,"The Parent relation is not configured correctly in the server side , some parents or options are null, dataelement uid: "+dataElement.getUid());
            return;
        }
        String[] parents = parentUids.split(PARENTTOKEN);
        String[] options = optionsUids.split(OPTIONTOKEN);
        for(int i=0; i<parents.length;i++){
            Log.d(TAG,"registerMultiLevelParentChildRelation: " +dataElementExtended.getDataElement().getUid());
            addDataElementParent(dataElementExtended, parents[i],options[i]);
        }
    }


    /**
     * Create and save the parent reation
     *
     * @param dataElementExtended
     */
    private void addDataElementParent(DataElementExtended dataElementExtended, String parent, String options) {
        String[] matchOptions = options.split(OPTIONSUBTOKEN);
        DataElement dataElement=dataElementExtended.getDataElement();
        Question childQuestion = mapQuestions.get(dataElement.getUid()+ dataElementExtended.getProgramUid());


        //get parentquestion
        Question parentQuestion = mapQuestions.get(parent+ dataElementExtended.getProgramUid());
        //get parentquestion options
        if(parentQuestion==null)
            Log.d(TAG, "the parent of "+ childQuestion.getUid()+ "is not downloaded (parentUid: "+parent);
        List<Option> parentOptions = parentQuestion.getAnswer().getOptions();
        for (Option option : parentOptions)
        {
            for(String matchOption:matchOptions) {
                if (matchOption.equals(option.getUid())) {
                    QuestionRelation questionRelation = new QuestionRelation();
                    questionRelation.setOperation(1);
                    questionRelation.setQuestion(childQuestion);
                    questionRelation.save();
                    Match match = new Match();
                    match.setQuestionRelation(questionRelation);
                    match.save();
                    new QuestionOption(option, parentQuestion, match).save();
                }
            }
        }
    }
    /**
     * Create QuestionOption QuestionRelation and Match relations
     * <p/>
     * checks if the dataElement is a parent(if is a parent it have mapMatchType and mapMatchLevel)
     * Later get the two childs and create the relation
     * it needs check what Options factors do match, and check it with method getMatchOption() .
     *
     * @param dataElementExtended
     */
    private void addQuestionRelations(DataElementExtended dataElementExtended) {

        DataElement dataElement=dataElementExtended.getDataElement();
        String programUid = dataElementExtended.getProgramUid();
        String matchRelationType = mapMatchType.get(programUid + dataElement.getUid());
        String matchRelationGroup = mapMatchLevel.get(programUid + dataElement.getUid());
        Question appQuestion = mapQuestions.get(dataElement.getUid()+programUid);

        if (matchRelationType != null && matchRelationType.equals(DataElementExtended.PARENT)) {
            List<String> mapChilds = mapMatchChilds.get(programUid + matchRelationGroup);
            List<Question> children = new ArrayList<>();
            children.add(mapQuestions.get(mapChilds.get(0)+programUid));
            children.add(mapQuestions.get(mapChilds.get(1)+programUid));

            if (mapQuestions.get(mapChilds.get(0)+programUid) != null && mapQuestions.get(mapChilds.get(1)+programUid) != null) {
                QuestionRelation questionRelation = new QuestionRelation();
                questionRelation.setOperation(0);
                questionRelation.setQuestion(appQuestion);
                questionRelation.save();
                questionRelation.createMatchFromQuestions(children);
            }
        }
    }

}
