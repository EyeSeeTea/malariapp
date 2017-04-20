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
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.entity.SurveyEntity;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    public static List<Feedback> build(SurveyEntity survey, String module){
        return build(survey, false, module);
    }

    /**
     * Builds an ordered list of feedback items for the given survey
     * @param survey
     * @param parents true for representing every composite, including parents, otherwise parents are removed
     * @return
     */
    public static List<Feedback> build(SurveyEntity survey, boolean parents, String module){
        List<Feedback> feedbackList=new ArrayList<>();
        //Prepare scores

        ProgramDB program = ProgramDB.findById(survey.getProgramEntity().getId());
        List<CompositeScoreDB> compositeScoreList= ScoreRegister.loadCompositeScores(survey.getId(), program, module);


        //Calculate main score
        survey.setMainScore(ScoreRegister.calculateMainScore(compositeScoreList,survey.getId(), module));

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

        //For each score add proper items
        for(CompositeScoreDB compositeScore:compositeScoreList){
            //add score
            float score = ScoreRegister.getCompositeScore(compositeScore, survey.getId(),
                        module);
            feedbackList.add(new CompositeScoreFeedback(compositeScore, score));

            //add its questions
            List<QuestionDB> questions=compositeScore.getQuestions();
            for(QuestionDB question:questions){
                if(!question.isHiddenBySurvey(survey.getId())) {
                    ValueDB valueInSurvey = question.getValueBySurvey(survey.getId());
                    feedbackList.add(new QuestionFeedback(question, valueInSurvey));
                }
            }
        }
        return feedbackList;
    }

}
