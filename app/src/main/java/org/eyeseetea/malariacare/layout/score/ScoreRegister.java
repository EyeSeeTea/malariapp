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
    public enum CalculationType {ALL_STEPS, NON_CRITICAL_STEPS}

    /**
     * Tag for logging
     */
    private static final String TAG = ".ScoreRegister";

    /**
     * Map of scores for each compositescore by survey and tag
     */
    public static final Map<String, Map<Float, Map<CompositeScoreDB, CompositeNumDenRecord>>>
            compositeScoreMapBySurvey = new HashMap<>();

    /**
     * Map of scores for each tab by survey and tag
     */
    public static final Map<String, Map<Float, Map<TabDB, TabNumDenRecord>>> tabScoreMap =
            new HashMap<>();


    public static void initScoresForQuestions(List<QuestionDB> questions, SurveyDB survey,
            String tag) {
        for (QuestionDB question : questions) {
            if (!question.isHiddenBySurvey(survey.getId_survey())) {
                question.initScore(survey.getId_survey(), tag);
            }
        }
    }

    public static void addRecord(QuestionDB question, Float num, Float den, float idSurvey,
            String tag) {
        if (question.getCompositeScore() != null && compositeScoreMapBySurvey.get(tag).get(
                idSurvey) != null) {
            compositeScoreMapBySurvey.get(tag).get(idSurvey).get(
                    question.getCompositeScore()).addRecord(question, num, den);
        }
        tabScoreMap.get(tag).get(idSurvey).get(question.getHeader().getTab()).addRecord(question,
                num, den);
    }

    public static void addQuestionRowRecords(QuestionRow questionRow, float idSurvey, String tag) {
        for (QuestionDB question : questionRow.getQuestions()) {
            ScoreRegister.addRecord(question, 0F, ScoreRegister.calcDenum(question, idSurvey),
                    idSurvey, tag);
        }

    }

    public static void deleteRecord(QuestionDB question, float idSurvey, String tag) {
        if (question.getCompositeScore() != null) {
            compositeScoreMapBySurvey.get(tag).get(idSurvey).get(
                    question.getCompositeScore()).deleteRecord(question);
        }
        tabScoreMap.get(tag).get(idSurvey).get(question.getHeader().getTab()).deleteRecord(
                question);
    }

    private static List<Float> getRecursiveScore(CompositeScoreDB cScore, List<Float> result,
            float idSurvey, String tag) {
        Log.d(TAG, " mod " + tag + " idsurvey " + idSurvey + " score " + cScore);
        //Protect from wrong server data
        if (compositeScoreMapBySurvey.get(tag) == null || compositeScoreMapBySurvey.get(
                tag).get(idSurvey) == null || compositeScoreMapBySurvey.get(
                tag).get(
                idSurvey).get(cScore) == null) {
            return Arrays.asList(0f, 0f);
        }

        //Sum its own records
        result = compositeScoreMapBySurvey.get(tag).get(idSurvey).get(cScore).calculateNumDenTotal(
                result);

        //Sum records from children scores
        for (CompositeScoreDB cScoreChildren : cScore.getCompositeScoreChildren()) {
            result = getRecursiveScore(cScoreChildren, result, idSurvey, tag);
        }
        return result;
    }

    public static List<Float> getNumDenum(QuestionDB question, float idSurvey, String tag) {
        if (!tabScoreMap.containsKey(tag)) {
            return null;
        }
        if (!tabScoreMap.get(tag).containsKey(idSurvey)) {
            return null;
        }
        if (!tabScoreMap.get(tag).get(idSurvey).containsKey(question.getHeader().getTab())) {
            return null;
        }
        return tabScoreMap.get(tag).get(idSurvey).get(
                question.getHeader().getTab()).getNumDenRecord().get(question);
    }

    public static Float getCompositeScore(CompositeScoreDB cScore, float idSurvey, String tag) {

        List<Float> result = getRecursiveScore(cScore, new ArrayList<>(Arrays.asList(0F, 0F)),
                idSurvey, tag);

        Log.d(TAG, String.format("getCompositeScore %s -> %s", cScore.getHierarchical_code(),
                result.toString()));
        return ScoreUtils.calculateScoreFromNumDen(result);
    }

    /**
     * Gets the list of numerators/denominator for a provided compositeScore, survey and tag.
     */
    public static List<Float> getCompositeScoreResult(CompositeScoreDB cScore, float idSurvey,
            String tag) {

        List<Float> result = getRecursiveScore(cScore, new ArrayList<>(Arrays.asList(0F, 0F)),
                idSurvey, tag);

        Log.d(TAG, String.format("getCompositeScore %s -> %s", cScore.getHierarchical_code(),
                result.toString()));

        return result;
    }

    public static List<Float> calculateGeneralScore(TabDB tab, float idSurvey, String tag) {
        return tabScoreMap.get(tag).get(idSurvey).get(tab).calculateTotal();
    }

    /**
     * Resets compositescores and initializes a new set of them
     */
    public static void registerCompositeScores(List<CompositeScoreDB> compositeScores,
            float idSurvey, String tag) {
        clearCompositeScoreByTagAndSurvey(idSurvey, tag);
        for (CompositeScoreDB compositeScore : compositeScores) {
            Log.i(TAG, "Register composite score: " + compositeScore.getHierarchical_code());
            if (!compositeScoreMapBySurvey.containsKey(tag)) {
                compositeScoreMapBySurvey.put(tag,
                        new HashMap<Float, Map<CompositeScoreDB, CompositeNumDenRecord>>());
            }
            if (!compositeScoreMapBySurvey.get(tag).containsKey(idSurvey)) {
                compositeScoreMapBySurvey.get(tag).put(idSurvey,
                        new HashMap<CompositeScoreDB, CompositeNumDenRecord>());
            }
            compositeScoreMapBySurvey.get(tag).get(idSurvey).put(compositeScore,
                    new CompositeNumDenRecord());
        }
    }


    /**
     * Remove the CompositeScores by survey and tag
     */
    public static void clearCompositeScoreByTagAndSurvey(float idSurvey, String tag) {
        if (compositeScoreMapBySurvey.containsKey(tag)) {
            if (compositeScoreMapBySurvey.get(tag).containsKey(idSurvey)) {
                compositeScoreMapBySurvey.get(tag).remove(idSurvey);
            }
        }
    }

    /**
     * Remove the CompositeScores by survey and tag
     */
    public static void clearTabMapsByTagAndSurvey(float idSurvey, String tag) {
        if (tabScoreMap.containsKey(tag)) {
            if (tabScoreMap.get(tag).containsKey(idSurvey)) {
                tabScoreMap.get(tag).remove(idSurvey);
            }
        }
    }

    /**
     * Resets generalScores and initializes a new set ot them
     */
    public static void registerTabScores(List<TabDB> tabs, float idSurvey, String tag) {
        clearTabMapsByTagAndSurvey(idSurvey, tag);
        for (TabDB tab : tabs) {
            Log.i(TAG, "Register tab score: " + tab.getName());
            if (!tabScoreMap.containsKey(tag)) {
                tabScoreMap.put(tag, new HashMap<Float, Map<TabDB, TabNumDenRecord>>());
            }
            if (!tabScoreMap.get(tag).containsKey(idSurvey)) {
                tabScoreMap.get(tag).put(idSurvey, new HashMap<TabDB, TabNumDenRecord>());
            }
            tabScoreMap.get(tag).get(idSurvey).put(tab, new TabNumDenRecord());
        }
    }

    /**
     * Clears every score in session
     */
    public static void clear(float idSurvey, String tag) {
        clearCompositeScoreByTagAndSurvey(idSurvey, tag);
        clearTabMapsByTagAndSurvey(idSurvey, tag);
    }

    /**
     * Calculates the numerator of the given question & survey
     * returns null is invalid question to the scoreregister and the question denominator will be
     * ignored too.
     */
    public static Float calcNum(QuestionDB question, float idSurvey) {
        if (question == null) {
            return null;
        }
        ValueDB value = question.getValueBySurvey(idSurvey);

        //If a question value is null it should be ignored, the question isn't be scored if it
        // have null num
        //Note: In case of the compulsory questions, that questions always have not null value,
        // it is controlled by the app workflow.
        if (value == null) {
            return null;
        }

        OptionDB option = question.getOptionBySurvey(idSurvey);
        if (option == null) {
            return 0f;
        }
        return question.getNumerator_w() * option.getFactor();
    }

    /**
     * Calculates the denominator of the given question & survey
     */
    public static float calcDenum(QuestionDB question, float idSurvey) {
        float result = 0;

        if (!question.isScored()) {
            return 0;
        }

        OptionDB option = question.getOptionBySurvey(idSurvey);
        if (option == null) {
            return calcDenum(0, question);
        }
        return calcDenum(option.getFactor(), question);
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

    public static List<CompositeScoreDB> loadCompositeScores(SurveyDB survey, String tag) {
        return loadCompositeScores (survey, tag, CalculationType.ALL_STEPS);
    }


    /**
     * Cleans, prepares, calculates and returns all the scores info for the given survey
     */
    public static List<CompositeScoreDB> loadCompositeScores(SurveyDB survey, String tag,
            CalculationType calculationType) {

        //Cleans score
        Log.d(TAG, "clean composite score " + survey.getId_survey() + " tag " + tag);
        ScoreRegister.clear(survey.getId_survey(), tag);
        Log.d(TAG, "load composite Score " + survey.getId_survey() + " tag " + tag);

        //Register scores for tabs
        List<TabDB> tabs = survey.getProgram().getTabs();
        ScoreRegister.registerTabScores(tabs, survey.getId_survey(), tag);

        //Register scores for composites
        List<CompositeScoreDB> compositeScoreList = CompositeScoreDB.listByProgram(
                survey.getProgram());
        ScoreRegister.registerCompositeScores(compositeScoreList, survey.getId_survey(), tag);

        List<QuestionDB> questions = QuestionDB.listByProgram(survey.getProgram());

        if (calculationType == CalculationType.NON_CRITICAL_STEPS){
            questions = getOnlyNonCriticalQuestions(questions);
        }

        //Initialize scores x question
        ScoreRegister.initScoresForQuestions(questions, survey, tag);

        Log.d(TAG, "Composite Score loaded " + survey.getId_survey() + " tag " + tag);
        return compositeScoreList;
    }

    private static List<QuestionDB> getOnlyNonCriticalQuestions(List<QuestionDB> questions) {
        List<QuestionDB> nonCriticalQuestions = new ArrayList<>();

        for (QuestionDB question: questions) {
            if (question.getCompulsory() == false){
                nonCriticalQuestions.add(question);
            }
        }

        return nonCriticalQuestions;
    }

    public static float calculateMainScore(List<CompositeScoreDB> scores, float idSurvey,
            String tag) {
        float sumScores = 0;
        float numParentScores = 0;
        for (CompositeScoreDB score : scores) {
            //only parent scores are interesting
            if (score.getComposite_score() == null) {
                List<Float> result = getCompositeScoreResult(score, idSurvey, tag);
                //count only the compositeScores with answers.
                if (result.get(1) > 0) {
                    sumScores += ScoreUtils.calculateScoreFromNumDen(result);
                    numParentScores++;
                }
            }
        }
        return (numParentScores == 0) ? 0 : sumScores / numParentScores;
    }


    public static float calculateMainScore(SurveyDB survey, String tag) {
        //Prepare all scores
        List<CompositeScoreDB> scores = loadCompositeScores(survey, tag);

        return calculateMainScore(scores, survey.getId_survey(), tag);
    }

    public static float calculateScoreForNonCriticalsSteps(SurveyDB survey, String tag) {
        List<CompositeScoreDB> scores =
                loadCompositeScores(survey, tag, CalculationType.NON_CRITICAL_STEPS);

        return calculateMainScore(scores, survey.getId_survey(), tag);
    }

    public static CompositeScoreDB getCompositeScoreRoot(List<CompositeScoreDB> compositeScores) {
        for(CompositeScoreDB compositeScoreDB : compositeScores){
            if(compositeScoreDB.hasParent()){
                return compositeScoreDB;
            }
        }
        return null;
    }
}
