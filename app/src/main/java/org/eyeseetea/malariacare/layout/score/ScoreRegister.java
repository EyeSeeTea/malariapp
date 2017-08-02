/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.score;

import android.util.Log;

import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.layout.utils.QuestionRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for storing and dealing with survey scores.
 */
public class ScoreRegister {

    /**
     * Tag for logging
     */
    private static final String TAG=".ScoreRegister";

    /**
     * Map of scores for each compositescore by survey and module
     */
    public static final Map<String, Map<Float, Map<CompositeScoreDB, CompositeNumDenRecord>>> compositeScoreMapBySurvey = new HashMap<>();

    /**
     * Map of scores for each tab by survey and module
     */
    public static final Map<String, Map<Float, Map<TabDB, TabNumDenRecord>>> tabScoreMap = new HashMap<>();
    public static void initScoresForQuestions(List<QuestionDB> questions, Long surveyId, String module){
        for(QuestionDB question : questions){
            if(!question.isHiddenBySurvey(surveyId)) {
                question.initScore(surveyId, module);
            }
        }
    }

    public static void addRecord(QuestionDB question, Float num, Float den, float idSurvey, String module){
         if (question.getCompositeScore() != null) {
             compositeScoreMapBySurvey.get(module).get(idSurvey).get(question.getCompositeScore()).addRecord(question, num, den);
        }
        tabScoreMap.get(module).get(idSurvey).get(question.getHeader().getTab()).addRecord(question, num, den);
    }

    public static void addQuestionRowRecords(QuestionRow questionRow, float idSurvey, String module){
        for(QuestionDB question:questionRow.getQuestions()){
            ScoreRegister.addRecord(question, 0F, ScoreRegister.calcDenum(question, idSurvey), idSurvey, module);
        }

    }

    public static void deleteRecord(QuestionDB question, float idSurvey, String module){
        if (question.getCompositeScore() != null)
            compositeScoreMapBySurvey.get(module).get(idSurvey).get(question.getCompositeScore()).deleteRecord(question);
        tabScoreMap.get(module).get(idSurvey).get(question.getHeader().getTab()).deleteRecord(question);
    }

    private static List<Float> getRecursiveScore(CompositeScoreDB cScore, List<Float> result, float idSurvey, String module) {
        Log.d(TAG, " mod "+ module +" idsurvey "+ idSurvey + " score "+ cScore);
        //Protect from wrong server data
        if (compositeScoreMapBySurvey.get(module).get(idSurvey).get(cScore)==null) {
            return Arrays.asList(0f,0f);
        }

        //Sum its own records
        result=compositeScoreMapBySurvey.get(module).get(idSurvey).get(cScore).calculateNumDenTotal(result);

        //Sum records from children scores
        for (CompositeScoreDB cScoreChildren : cScore.getCompositeScoreChildren()) {
            result = getRecursiveScore(cScoreChildren, result, idSurvey, module);
        }
        return result;
    }

    public static List<Float> getNumDenum(QuestionDB question, float idSurvey, String module) {
        if(!tabScoreMap.containsKey(module))
            return null;
        if(!tabScoreMap.get(module).containsKey(idSurvey))
            return null;
        if(!tabScoreMap.get(module).get(idSurvey).containsKey(question.getHeader().getTab()))
            return null;
        return tabScoreMap.get(module).get(idSurvey).get(question.getHeader().getTab()).getNumDenRecord().get(question);
    }

    public static Float getCompositeScore(CompositeScoreDB cScore, float idSurvey, String module) {

        List<Float>result= getRecursiveScore(cScore, new ArrayList<>(Arrays.asList(0F, 0F)), idSurvey, module);

        Log.d(TAG,String.format("getCompositeScore %s -> %s",cScore.getHierarchical_code(),result.toString()));
        return ScoreUtils.calculateScoreFromNumDen(result);
    }

    /**
     * Gets the list of numerators/denominator for a provided compositeScore, survey and module.
     * @param cScore
     * @param idSurvey
     * @param module
     */
    public static List<Float> getCompositeScoreResult(CompositeScoreDB cScore, float idSurvey, String module) {

        List<Float>result= getRecursiveScore(cScore, new ArrayList<>(Arrays.asList(0F, 0F)), idSurvey, module);

        Log.d(TAG,String.format("getCompositeScore %s -> %s",cScore.getHierarchical_code(),result.toString()));

        return result;
    }

    public static List<Float> calculateGeneralScore(TabDB tab, float idSurvey, String module) {
        return tabScoreMap.get(module).get(idSurvey).get(tab).calculateTotal();
    }

    /**
     * Resets compositescores and initializes a new set of them
     * @param compositeScores
     */
    public static void registerCompositeScores(List<CompositeScoreDB> compositeScores, float idSurvey, String module){
        clearCompositeScoreByModuleAndSurvey(idSurvey,module);
        for(CompositeScoreDB compositeScore:compositeScores){
            Log.i(TAG, "Register composite score: " + compositeScore.getHierarchical_code());
            if(!compositeScoreMapBySurvey.containsKey(module)) {
                compositeScoreMapBySurvey.put(module, new HashMap<Float, Map<CompositeScoreDB, CompositeNumDenRecord>>());
            }
            if(!compositeScoreMapBySurvey.get(module).containsKey(idSurvey))
                compositeScoreMapBySurvey.get(module).put(idSurvey,new HashMap<CompositeScoreDB, CompositeNumDenRecord>());
            compositeScoreMapBySurvey.get(module).get(idSurvey).put(compositeScore, new CompositeNumDenRecord());
        }
    }


    /**
     * Remove the CompositeScores by survey and module
     * @param idSurvey
     * @param module
     */
    public static void clearCompositeScoreByModuleAndSurvey(float idSurvey, String module){
        if(compositeScoreMapBySurvey.containsKey(module))
            if(compositeScoreMapBySurvey.get(module).containsKey(idSurvey)) {
                compositeScoreMapBySurvey.get(module).remove(idSurvey);
            }
    }

    /**
     * Remove the CompositeScores by survey and module
     * @param idSurvey
     * @param module
     */
    public static void clearTabMapsByModuleAndSurvey(float idSurvey, String module){
        if(tabScoreMap.containsKey(module))
            if(tabScoreMap.get(module).containsKey(idSurvey)) {
                tabScoreMap.get(module).remove(idSurvey);
            }
    }
    /**
     * Resets generalScores and initializes a new set ot them
     * @param tabs
     */
    public static void registerTabScores(List<TabDB> tabs, float idSurvey, String module){
        clearTabMapsByModuleAndSurvey(idSurvey, module);
        for(TabDB tab:tabs){
            Log.i(TAG, "Register tab score: " + tab.getName());
            if(!tabScoreMap.containsKey(module))
                tabScoreMap.put(module, new HashMap<Float, Map<TabDB, TabNumDenRecord>>());
            if(!tabScoreMap.get(module).containsKey(idSurvey))
                tabScoreMap.get(module).put(idSurvey,  new HashMap<TabDB, TabNumDenRecord>());
            tabScoreMap.get(module).get(idSurvey).put(tab, new TabNumDenRecord());
        }
    }

    /**
     * Clears every score in session
     */
    public static void clear(float idSurvey, String module){
        clearCompositeScoreByModuleAndSurvey(idSurvey, module);
        clearTabMapsByModuleAndSurvey(idSurvey,module);
    }

    /**
     * Calculates the numerator of the given question & survey
     * returns null is invalid question to the scoreregister and the question denominator will be ignored too.
     * @param question
     * @param idSurvey
     * @return
     */
    public static Float calcNum(QuestionDB question, float idSurvey) {
        if (question == null) {
            return null;
        }
        ValueDB value = question.getValueBySurvey(idSurvey);

        //If a question value is null it should be ignored, the question isn't be scored if it have null num
        //Note: In case of the compulsory questions, that questions always have not null value, it is controlled by the app workflow.
        if(value == null){
            return null;
        }

        OptionDB option=question.getOptionBySurvey(idSurvey);
        if(option==null){
            return 0f;
        }
        return question.getNumerator_w()*option.getFactor();
    }

    /**
     * Calculates the denominator of the given question & survey
     * @param question
     * @param idSurvey
     * @return
     */
    public static float calcDenum(QuestionDB question,float idSurvey) {
        float result = 0;

        if(!question.isScored()){
            return 0;
        }

        OptionDB option = question.getOptionBySurvey(idSurvey);
        if(option==null){
            return calcDenum(0,question);
        }
        return calcDenum(option.getFactor(),question);
    }

    private static float calcDenum(float factor, QuestionDB question) {
        float num = question.getNumerator_w();
        float denum = question.getDenominator_w();

        if (num == denum) {
            return denum;
        }
        if (num == 0 && denum != 0) {
            return factor * denum;
        }
        return 0;
    }

    /**
     * Cleans, prepares, calculates and returns all the scores info for the given survey
     * @param surveyId
     * @return
     */
    public static List<CompositeScoreDB> loadCompositeScores(Long surveyId, ProgramDB program, String module){
        //Cleans score
        Log.d(TAG, "clean composite score "+ surveyId + " module " + module);
        ScoreRegister.clear(surveyId, module);
        Log.d(TAG, "load composite Score "+ surveyId + " module " + module);
        //Register scores for tabs
        List<TabDB> tabs= program.getTabs();
        ScoreRegister.registerTabScores(tabs, surveyId, module);

        //Register scores for composites
        List<CompositeScoreDB> compositeScoreList=CompositeScoreDB.listByProgram(program.getId_program());
        ScoreRegister.registerCompositeScores(compositeScoreList, surveyId, module);
        //Initialize scores x question
        ScoreRegister.initScoresForQuestions(QuestionDB.listByProgram(program), surveyId, module);

        Log.d(TAG, "Composite Score loaded "+ surveyId + " module " + module);
        return compositeScoreList;
    }

    public static float calculateMainScore(List<CompositeScoreDB> scores, float idSurvey, String module){
        float sumScores=0;
        float numParentScores=0;
        for(CompositeScoreDB score:scores){
            //only parent scores are interesting
            if(score.getComposite_score()==null){
                List<Float> result=getCompositeScoreResult(score, idSurvey, module);
                //count only the compositeScores with answers.
                if(result.get(1)>0) {
                    sumScores += ScoreUtils.calculateScoreFromNumDen(result);
                    numParentScores++;
                }
            }
        }
        return (numParentScores==0) ? 0 : sumScores/numParentScores;
    }


    public static float calculateMainScore(Long surveyId, String module){
        //Prepare all scores
        ProgramDB program = ProgramDB.findBySurveyId(surveyId);
        List<CompositeScoreDB> scores = loadCompositeScores(surveyId, program, module);

        return calculateMainScore(scores, surveyId, module);
    }

}
