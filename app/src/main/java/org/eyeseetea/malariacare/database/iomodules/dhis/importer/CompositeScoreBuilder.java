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
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.Option;

import java.util.HashMap;
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
     * Holds every compositeScore to calculate its order and parent
     */
    static Map<String,CompositeScore> mapCompositeScores;

    /**
     * Helper required to deal with AttributeValues
     */
    AttributeValueHelper attributeValueHelper;

    CompositeScoreBuilder(){
        mapCompositeScores=new HashMap<>();
        attributeValueHelper=new AttributeValueHelper();

        Option optionCompositeScore=attributeValueHelper.findOptionByName(COMPOSITE_SCORE_NAME);
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
        mapCompositeScores.put(compositeScore.getUid(), compositeScore);
        mapCompositeScores.put(compositeScore.getHierarchical_code(), compositeScore);
    }

    /**
     * Builds scores fulfilling its order and parent attributes.
     * This operation requires that every composite score has already been registered and procesed and thus It cant be done during 'visit'
     */
    public void buildScores(){
        buildOrder();
        buildHierarchy();
    }

    /**
     * Checks whether a dataElement is a question or a compositescore
     * @param dataElement
     * @return
     */
    public boolean isACompositeScore(DataElement dataElement){

        String typeQuestion=attributeValueHelper.findAttributeValueByCode(ATTRIBUTE_QUESTION_TYPE_CODE,dataElement);

        if(typeQuestion==null){
            return false;
        }

        return typeQuestion.equals(COMPOSITE_SCORE_CODE);
    }

    public String findHierarchicalCode(DataElement dataElement){
        //Not a composite -> done
        if(!isACompositeScore(dataElement)){
            return null;
        }

        //Find the value of the attribute 'DECompositiveScore' for this dataElement
        return attributeValueHelper.findAttributeValueByCode(ATTRIBUTE_COMPOSITE_SCORE_CODE,dataElement);
    }

    /**
     * Completes the orderBy attribute in compositeScore according to its hierarchical code
     */
    private void buildOrder(){
        //
    }

    /**
     * Registers a compositeScore in builder
     */
    private void buildHierarchy(){

    }

}
