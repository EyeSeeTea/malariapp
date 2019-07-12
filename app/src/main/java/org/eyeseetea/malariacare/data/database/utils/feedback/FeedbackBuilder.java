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

package org.eyeseetea.malariacare.data.database.utils.feedback;

import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Little builder thats creates an ordered list of feedback items
 * Created by arrizabalaga on 14/09/15.
 */
public class FeedbackBuilder {

    /**
     * Builds an ordered list of feedback items for the given survey
     * @param survey
     * @return
     */
    public static List<Feedback> build(SurveyDB survey, String module){
        return build(survey, false, module);
    }

    /**
     * Builds an ordered list of feedback items for the given survey
     * @param survey
     * @param parents true for representing every composite, including parents, otherwise parents are removed
     * @return
     */
    public static List<Feedback> build(SurveyDB survey, boolean parents, String module){
        List<Feedback> feedbackList=new ArrayList<>();
        //Prepare scores
        List<CompositeScoreDB> compositeScoreList= ScoreRegister.loadCompositeScores(survey, module);


        //Calculate main score
        survey.setMainScore(survey.getId_survey(),
                ScoreRegister.getCompositeScoreRoot(compositeScoreList).getUid(),
                ScoreRegister.calculateMainScore(compositeScoreList, survey.getId_survey(), module));

        if (!parents) {
            //Remove parents from list (to avoid showing the parent composite that is there just to push the overall score)
            for (Iterator<CompositeScoreDB> iterator = compositeScoreList.iterator(); iterator.hasNext(); ) {
                CompositeScoreDB compositeScore = iterator.next();
                //Show only if a parent have questions.
                if(compositeScore.getQuestions().size()<1) {
                    if (!compositeScore.hasParent()) iterator.remove();
                }
            }
        }
        Map<String, CompositeScoreFeedback> compositeScoreFeedbacks = new HashMap<>();

        //For each score add proper items
        for(CompositeScoreDB compositeScore:compositeScoreList){
            //add score
            float score = ScoreRegister.getCompositeScore(compositeScore, survey.getId_survey(),
                        module);
            CompositeScoreFeedback compositeScoreFeedback = new CompositeScoreFeedback(compositeScore, score, !compositeScore.getHierarchical_code().contains("."));
            String hierarchicalCode = compositeScore.getHierarchical_code();
            compositeScoreFeedbacks.put(hierarchicalCode, compositeScoreFeedback);
            if(hierarchicalCode.contains(".")) {
                int lastIndex = hierarchicalCode.lastIndexOf(".");
                String parentCS = hierarchicalCode.substring(0, lastIndex);
                if (compositeScoreFeedbacks.containsKey(parentCS)) {
                    compositeScoreFeedbacks.get(parentCS).addCompositeScoreFeedbackList(compositeScoreFeedback);
                }
            }
            feedbackList.add(compositeScoreFeedback);

            //add its questions
            List<QuestionFeedback> questionFeedbacks = new ArrayList<>();
            List<QuestionDB> questions=compositeScore.getQuestions();
            for(QuestionDB question:questions){
                if(!question.isHiddenBySurvey(survey.getId_survey())) {
                    ValueDB valueInSurvey = question.getValueBySurvey(survey.getId_survey());
                    QuestionFeedback questionFeedback = new QuestionFeedback(question, valueInSurvey);
                    feedbackList.add(questionFeedback);
                    questionFeedbacks.add(questionFeedback);
                }
            }
            compositeScoreFeedback.setFeedbackList(questionFeedbacks);

        }
        return feedbackList;
    }

}
