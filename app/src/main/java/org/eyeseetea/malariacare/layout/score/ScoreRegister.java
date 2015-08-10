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
import org.eyeseetea.malariacare.utils.Constants;

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
     * Map of scores for each compositescore
     */
    private static final Map<CompositeScore, CompositeNumDenRecord> compositeScoreMap = new HashMap<CompositeScore, CompositeNumDenRecord>();

    /**
     * Map of scores for each tab
     */
    private static final Map<Tab, TabNumDenRecord> tabScoreMap = new HashMap<Tab, TabNumDenRecord>();

    public static void initScoresForQuestions(List<Question> questions, Survey survey){
        for(Question question : questions){
            if(!question.isHiddenBySurvey(survey)) {
                question.initScore(survey);
            }else{
                addRecord(question, 0F, calcDenum(question));
            }
        }
    }

    public static void addRecord(Question question, Float num, Float den){
        if (question.getCompositeScore() != null && compositeScoreMap.get(question.getCompositeScore())!=null) {
            compositeScoreMap.get(question.getCompositeScore()).addRecord(question, num, den);
        }
        if (tabScoreMap.get(question.getHeader().getTab())!=null)
        tabScoreMap.get(question.getHeader().getTab()).addRecord(question, num, den);
    }

    public static void deleteRecord(Question question){
        if (question.getCompositeScore() != null)
            compositeScoreMap.get(question.getCompositeScore()).deleteRecord(question);
        tabScoreMap.get(question.getHeader().getTab()).deleteRecord(question);
    }

    private static List<Float> getRecursiveScore(CompositeScore cScore, List<Float> result) {

        if (!cScore.hasChildren()) {

            //FIXME this try catch just covers a error in data compositeScore: '4.2'
            try{
                return compositeScoreMap.get(cScore).calculateNumDenTotal(result);
            }catch(Exception ex){
                return Arrays.asList(new Float(0f),new Float(0f));
            }
        }else {
            for (CompositeScore cScoreChildren : cScore.getCompositeScoreChildren())
                result = getRecursiveScore(cScoreChildren, result);
            return result;
        }
    }

    public static List<Float> getNumDenum(Question question) {
        return tabScoreMap.get(question.getHeader().getTab()).getNumDenRecord().get(question);
    }

    public static Float getCompositeScore(CompositeScore cScore) {

        List<Float>result = compositeScoreMap.get(cScore).calculateNumDenTotal(new ArrayList<Float>(Arrays.asList(0F, 0F)));

        result = getRecursiveScore(cScore, result);

        return ScoreUtils.calculateScoreFromNumDen(result);
    }


    public static List<Float> calculateGeneralScore(Tab tab) {
        return tabScoreMap.get(tab).calculateTotal();
    }

    /**
     * Resets compositescores and initializes a new set of them
     * @param compositeScores
     */
    public static void registerCompositeScores(List<CompositeScore> compositeScores){
        compositeScoreMap.clear();
        for(CompositeScore compositeScore:compositeScores){
            Log.i(TAG, "Register composite score: " + compositeScore.getCode());
            compositeScoreMap.put(compositeScore, new CompositeNumDenRecord());
        }
    }

    /**
     * Resets generalScores and initializes a new set ot them
     * @param tabs
     */
    public static void registerTabScores(List<Tab> tabs){
        tabScoreMap.clear();
        for(Tab tab:tabs){
            Log.i(TAG, "Register tab score: " + tab.getName());
            tabScoreMap.put(tab, new TabNumDenRecord());
        }
    }

    /**
     * Clears every score in session
     */
    public static void clear(){
        compositeScoreMap.clear();
        tabScoreMap.clear();
    }

    /**
     * Calculates the numerator of the given question in the current survey
     * @param question
     * @return
     */
    public static float calcNum(Question question) {
        return calcNum(question,Session.getSurvey());
    }

    /**
     * Calculates the numerator of the given question & survey
     * @param question
     * @param survey
     * @return
     */
    public static float calcNum(Question question, Survey survey){
        if(survey==null || question==null){
            return 0;
        }

        Option option=question.getOptionBySurvey(survey);
        if(option==null){
            return 0;
        }
        return question.getNumerator_w()*option.getFactor();
    }

    /**
     * Calculates the numerator of the given question in the current survey
     * @param question
     * @return
     */
    public static float calcDenum(Question question) {
        return calcDenum(question,Session.getSurvey());
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

}
