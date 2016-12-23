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

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.layout.utils.QuestionRow;
import org.eyeseetea.malariacare.utils.AUtils;

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
    public static final Map<String, Map<Float, Map<CompositeScore, CompositeNumDenRecord>>> compositeScoreMapBySurvey = new HashMap<>();

    /**
     * Map of scores for each tab by survey and module
     */
    public static final Map<String, Map<Float, Map<Tab, TabNumDenRecord>>> tabScoreMap = new HashMap<>();


    public static void initScoresForQuestions(List<Question> questions, Survey survey, String module){
        for(Question question : questions){
            if(!question.isHiddenBySurvey(survey.getId_survey())) {
                question.initScore(survey, module);
            }
        }
    }

    public static void addRecord(Question question, Float num, Float den, float idSurvey, String module){
         if (question.getCompositeScore() != null) {
             compositeScoreMapBySurvey.get(module).get(idSurvey).get(question.getCompositeScore()).addRecord(question, num, den);
        }
        tabScoreMap.get(module).get(idSurvey).get(question.getHeader().getTab()).addRecord(question, num, den);
    }

    public static void addQuestionRowRecords(QuestionRow questionRow, float idSurvey, String module){
        for(Question question:questionRow.getQuestions()){
            ScoreRegister.addRecord(question, 0F, ScoreRegister.calcDenum(question, idSurvey), idSurvey, module);
        }

    }

    public static void deleteRecord(Question question, float idSurvey, String module){
        if (question.getCompositeScore() != null)
            compositeScoreMapBySurvey.get(module).get(idSurvey).get(question.getCompositeScore()).deleteRecord(question);
        tabScoreMap.get(module).get(idSurvey).get(question.getHeader().getTab()).deleteRecord(question);
    }

    private static List<Float> getRecursiveScore(CompositeScore cScore, List<Float> result, float idSurvey, String module) {

        //Protect from wrong server data
        if (compositeScoreMapBySurvey.get(module).get(idSurvey).get(cScore)==null) {
            return Arrays.asList(0f,0f);
        }

        //Sum its own records
        result=compositeScoreMapBySurvey.get(module).get(idSurvey).get(cScore).calculateNumDenTotal(result);

        //Sum records from children scores
        for (CompositeScore cScoreChildren : cScore.getCompositeScoreChildren()) {
            result = getRecursiveScore(cScoreChildren, result, idSurvey, module);
        }
        return result;
    }

    public static List<Float> getNumDenum(Question question, float idSurvey, String module) {
        if(!tabScoreMap.containsKey(module))
            return null;
        if(!tabScoreMap.get(module).containsKey(idSurvey))
            return null;
        if(!tabScoreMap.get(module).get(idSurvey).containsKey(question.getHeader().getTab()))
            return null;
        return tabScoreMap.get(module).get(idSurvey).get(question.getHeader().getTab()).getNumDenRecord().get(question);
    }

    public static Float getCompositeScore(CompositeScore cScore, float idSurvey, String module) {

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
    public static List<Float> getCompositeScoreResult(CompositeScore cScore, float idSurvey, String module) {

        List<Float>result= getRecursiveScore(cScore, new ArrayList<>(Arrays.asList(0F, 0F)), idSurvey, module);

        Log.d(TAG,String.format("getCompositeScore %s -> %s",cScore.getHierarchical_code(),result.toString()));

        return result;
    }

    public static List<Float> calculateGeneralScore(Tab tab, float idSurvey, String module) {
        return tabScoreMap.get(module).get(idSurvey).get(tab).calculateTotal();
    }

    /**
     * Resets compositescores and initializes a new set of them
     * @param compositeScores
     */
    public static void registerCompositeScores(List<CompositeScore> compositeScores, float idSurvey, String module){
        clearCompositeScoreByModuleAndSurvey(idSurvey,module);
        for(CompositeScore compositeScore:compositeScores){
            Log.i(TAG, "Register composite score: " + compositeScore.getHierarchical_code());
            if(!compositeScoreMapBySurvey.containsKey(module)) {
                compositeScoreMapBySurvey.put(module, new HashMap<Float, Map<CompositeScore, CompositeNumDenRecord>>());
            }
            if(!compositeScoreMapBySurvey.get(module).containsKey(idSurvey))
                compositeScoreMapBySurvey.get(module).put(idSurvey,new HashMap<CompositeScore, CompositeNumDenRecord>());
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
    public static void registerTabScores(List<Tab> tabs, float idSurvey, String module){
        clearTabMapsByModuleAndSurvey(idSurvey, module);
        for(Tab tab:tabs){
            Log.i(TAG, "Register tab score: " + tab.getName());
            if(!tabScoreMap.containsKey(module))
                tabScoreMap.put(module, new HashMap<Float, Map<Tab, TabNumDenRecord>>());
            if(!tabScoreMap.get(module).containsKey(idSurvey))
                tabScoreMap.get(module).put(idSurvey,  new HashMap<Tab, TabNumDenRecord>());
            tabScoreMap.get(module).get(idSurvey).put(tab, new TabNumDenRecord());
        }
    }

    /**
     * Clears every score in session
     */
    public static void clear(Survey survey, String module){
        if(survey==null)
            return;
        clearCompositeScoreByModuleAndSurvey(survey.getId_survey(), module);
        clearTabMapsByModuleAndSurvey(survey.getId_survey(),module);
    }

    /**
     * Calculates the numerator of the given question & survey
     * returns null is invalid question to the scoreregister and the question denominator will be ignored too.
     * @param question
     * @param survey
     * @return
     */
    public static Float calcNum(Question question, Survey survey) {
        if (survey==null || question == null) {
            return null;
        }
        Value value = question.getValueBySurvey(survey);

        //If a question value is null it should be ignored, the question isn't be scored if it have null num
        //Note: In case of the compulsory questions, that questions always have not null value, it is controlled by the app workflow.
        if(value == null){
            return null;
        }

        Option option=question.getOptionBySurvey(survey);
        if(option==null){
            return 0f;
        }
        return question.getNumerator_w()*option.getFactor();
    }

    /**
     * Calculates the numerator of the given question & survey
     * returns null is invalid question to the scoreregister and the question denominator will be ignored too.
     * @param question
     * @param idsurvey
     * @return
     */
    public static Float calcNum(Question question, float idsurvey) {
        if (question == null) {
            return null;
        }
        Value value = question.getValueBySurveyId(idsurvey);

        //If a question value is null it should be ignored, the question isn't be scored if it have null num
        //Note: In case of the compulsory questions, that questions always have not null value, it is controlled by the app workflow.
        if(value == null){
            return null;
        }

        Option option=question.getOptionBySurveyId(idsurvey);
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
    public static float calcDenum(Question question,float idSurvey) {
        float result = 0;

        if(!question.isScored()){
            return 0;
        }

        Option option = question.getOptionBySurveyId(idSurvey);
        if(option==null){
            return calcDenum(0,question);
        }
        return calcDenum(option.getFactor(),question);
    }
    /**
     * Calculates the denominator of the given question & survey
     * @param question
     * @param survey
     * @return
     */
    public static float calcDenum(Question question,Survey survey) {
        float result = 0;

        if(!question.isScored()){
            return 0;
        }

        Option option = question.getOptionBySurvey(survey);
        if(option==null){
            return calcDenum(0,question);
        }
        return calcDenum(option.getFactor(),question);
    }

    private static float calcDenum(float factor, Question question) {
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
     * @param survey
     * @return
     */
    public static List<CompositeScore> loadCompositeScores(Survey survey, String module){
        //Cleans score
        ScoreRegister.clear(survey, module);

        //Register scores for tabs
        List<Tab> tabs=survey.getProgram().getTabs();
        ScoreRegister.registerTabScores(tabs, survey.getId_survey(), module);

        //Register scores for composites
        List<CompositeScore> compositeScoreList=CompositeScore.listByProgram(survey.getProgram(),null);
        ScoreRegister.registerCompositeScores(compositeScoreList, survey.getId_survey(), module);
        //Initialize scores x question
        ScoreRegister.initScoresForQuestions(Question.listByProgram(survey.getProgram(),null), survey, module);
        
        return compositeScoreList;
    }
    /**
     * Cleans, prepares, calculates and returns all the scores info for the given survey
     * @param survey
     * @return
     */
    public static List<CompositeScore> loadCompositeScoresFromMemory(Survey survey, List<Question> questions, String module){
        //Cleans score
        ScoreRegister.clear(Session.getSurveyByModule(module),module);

        //Register scores for tabs
        List<Tab> tabs=survey.getProgram().getTabs();
        ScoreRegister.registerTabScores(tabs,survey.getId_survey(),module);

        //Register scores for composites
        List<CompositeScore> compositeScoreList=CompositeScore.listByProgram(survey.getProgram(),questions);
        ScoreRegister.registerCompositeScores(compositeScoreList,survey.getId_survey(),module);

        //Initialize scores x question
        ScoreRegister.initScoresForQuestions(Question.listByProgram(survey.getProgram(),questions),survey,module);


        return compositeScoreList;
    }

    public static float calculateMainScore(List<CompositeScore> scores, float idSurvey, String module){
        float sumScores=0;
        float numParentScores=0;
        for(CompositeScore score:scores){
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
        return sumScores/numParentScores;
    }

    public static float calculateMainScore(Survey survey, String module){
        //Prepare all scores
        List<CompositeScore> scores = loadCompositeScores(survey, module);

        return calculateMainScore(scores, survey.getId_survey(), module);
    }

}
