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

import com.google.common.base.Joiner;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionExtended;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement$Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by arrizabalaga on 13/11/15.
 */
public class CompositeScoreBuilder {

    private static final String TAG=".CompositeScoreBuilder";

    /**
     * Expected value for the attributeValue DeQuesType in those dataElements which are a CompositeScore
     */
    private static final String COMPOSITE_SCORE_NAME ="COMPOSITE_SCORE";

    /**
     * Hierarquical code for any root compositeScore
     */
    public static final String ROOT_NODE_CODE = "0";
    /**
     * Value of option 'COMPOSITE_SCORE'
     */
    private static String COMPOSITE_SCORE_CODE;
    /**
     * Code of attribute '20 Question Type'
     */
    private static final String ATTRIBUTE_QUESTION_TYPE_CODE ="DEQuesType";
    /**
     * Code of attribute '35 Composite Score'
     */
    private static final String ATTRIBUTE_COMPOSITE_SCORE_CODE ="DECompositiveScore";

    /**
     * Holds every compositeScore to calculate its order and parent according to its programStage and hierarchicalCode
     * programstageId -> hierarchical code -> Score
     */
    static Map<String,Map<String,CompositeScore>> mapCompositeScores;

    CompositeScoreBuilder(){
        mapCompositeScores=new HashMap<>();

        Option optionCompositeScore= OptionExtended.findOptionByName(COMPOSITE_SCORE_NAME);
        //No Option with COMPOSITE_SCORE -> error
        if(optionCompositeScore==null){
            Log.e(TAG,"There is no option named 'COMPOSITE_SCORE' which is a severe data error");
        }
        COMPOSITE_SCORE_CODE=optionCompositeScore.getCode();
    }

    /**
     * Registers a compositeScore in builder
     */
    public void add(CompositeScore compositeScore){

        //Find the right 'tabgroup' to group scores by program
        String programStageUID=findProgramStageByDataElementUID(compositeScore.getUid());
        if(programStageUID==null){
            Log.e(TAG,String.format("Cannot find tabgroup for composite score: %s",compositeScore.getLabel()));
            return;
        }

        //Get the map of scores for that program&
        Map<String,CompositeScore> compositeScoresXProgram=mapCompositeScores.get(programStageUID);
        if(compositeScoresXProgram==null){
            compositeScoresXProgram = new HashMap<String, CompositeScore>();
            mapCompositeScores.put(programStageUID, compositeScoresXProgram);
        }

        //Annotate the composite score in its proper map (x tabgroup|program)
        compositeScoresXProgram.put(compositeScore.getHierarchical_code(), compositeScore);
    }

    /**
     * Builds scores fulfilling its order and parent attributes.
     * This operation requires that every composite score has already been registered and procesed and thus It cant be done during 'visit'
     */
    public void buildScores(){
        for(Map.Entry<String,Map<String,CompositeScore>> mapXProgramStage: mapCompositeScores.entrySet()){
            buildOrder(mapXProgramStage.getValue());
            buildHierarchy(mapXProgramStage.getValue());
        }
    }

    /**
     * Finds the type of question for the given dataElementExtended
     * @param dataElementExtended
     * @return
     */
    public int findAnswerOutput(DataElementExtended dataElementExtended){
        String typeQuestion=dataElementExtended.findAttributeValueByCode(ATTRIBUTE_QUESTION_TYPE_CODE);

        //Not found -> error type question
        if(typeQuestion==null  || typeQuestion.equals(COMPOSITE_SCORE_CODE)){
            return Answer.DEFAULT_ANSWER_OUTPUT;
        }

        return Integer.valueOf(typeQuestion);
    }

    public String findHierarchicalCode(DataElementExtended dataElementExtended){

        //Not a composite -> done
        if(dataElementExtended==null || !dataElementExtended.isCompositeScore()){
            Log.d("CompositeScoreBuilder", String.format("dataElement %s is not CompositeScore", dataElementExtended.getDataElement().getUid()));
            return null;
        }

        //Find the value of the attribute 'DECompositiveScore' for this dataElement
        return dataElementExtended.findAttributeValueByCode(ATTRIBUTE_COMPOSITE_SCORE_CODE);
    }

    /**
     * Completes the orderBy attribute in compositeScore according to its hierarchical code
     */
    private void buildOrder(Map<String,CompositeScore> compositeScoreMap){

        //Order scores by its hierarchical code
        List<CompositeScore> scores = new ArrayList<CompositeScore>(compositeScoreMap.values());
        Collections.sort(scores,new CompositeScoreComparator());

        int i=0;
        for(CompositeScore score:scores){
            score.setOrder_pos(Integer.valueOf(i));
            score.save();
            i++;
        }

    }

    /**
     * Registers a compositeScore in builder
     */
    private void buildHierarchy(Map<String,CompositeScore> compositeScoreMap){
        CompositeScore rootScore=compositeScoreMap.get(ROOT_NODE_CODE);

        //Find the parent of each score
        for(CompositeScore compositeScore:compositeScoreMap.values()){

            //Hierarchical code
            String compositeScoreHierarchicalCode=compositeScore.getHierarchical_code();

            //Root node has no parent
            if(ROOT_NODE_CODE.equals(compositeScoreHierarchicalCode)){
                continue;
            }
            //Split the hirarchical code in levels (5.11.1 -> [0]=5 [1]=11 [2]=1)
            List<String> hierarchicalCodeLevels = Arrays.asList(compositeScoreHierarchicalCode.split("\\."));
            //0 levels -> parent: root | X level -> parent is the levels minus last
            String parentHierarchicalCode="";
            if(hierarchicalCodeLevels==null || hierarchicalCodeLevels.size()==0) {
                parentHierarchicalCode = ROOT_NODE_CODE;
            } else {
                parentHierarchicalCode = Joiner.on(".").skipNulls().join(hierarchicalCodeLevels.subList(0, hierarchicalCodeLevels.size()-1));
            }
            if(parentHierarchicalCode.equals(""))
                parentHierarchicalCode = ROOT_NODE_CODE;

            //Remove last dot if exist.
            compositeScore.setCompositeScore(compositeScoreMap.get(parentHierarchicalCode));
            compositeScore.save();
        }

    }

    /**
     * Find the associated prgoramStage (tabgroup) given a dataelement UID
     * @param dataElementUID
     * @return
     */
    private static String findProgramStageByDataElementUID(String dataElementUID){
        //Find the right 'tabgroup' to group scores by program
        ProgramStageDataElement programStageDataElement = new Select().from(ProgramStageDataElement.class)
                .indexedBy("ProgramStageDataElement_DataElement")
                .where(Condition.column(ProgramStageDataElement$Table.DATAELEMENT).is(dataElementUID)).orderBy(true, ProgramStageDataElement$Table.SORTORDER).querySingle();

        if(programStageDataElement==null){
            return null;
        }

        return programStageDataElement.getProgramStage();
    }

    /**
     * Comparator that order composite score by its hierarchical code
     */
    class CompositeScoreComparator implements Comparator<CompositeScore>{

        @Override
        public int compare(CompositeScore lhs, CompositeScore rhs) {
            return lhs.getOrder_pos().compareTo(rhs.getOrder_pos());
        }

        @Override
        public boolean equals(Object object) {
            return false;
        }
    }
    public static CompositeScore getCompositeScoreFromDataElementAndHierarchicalCode(DataElement dataElement, String HierarchicalCode){
        CompositeScore compositeScore=null;
        String programId= findProgramStageByDataElementUID(dataElement.getUid());
        Map<String,CompositeScore> compositeScoresInProgram=mapCompositeScores.get(programId);
        compositeScore=compositeScoresInProgram.get(HierarchicalCode);
        return compositeScore;
    }
}
