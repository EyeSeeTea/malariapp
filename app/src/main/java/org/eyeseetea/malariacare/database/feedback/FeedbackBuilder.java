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

package org.eyeseetea.malariacare.database.feedback;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;

import java.util.ArrayList;
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
    public static List<Feedback> build(Survey survey){
        List<Feedback> feedbackList=new ArrayList<>();
        //Prepare scores
        List<CompositeScore> compositeScoreList= ScoreRegister.loadCompositeScores(Session.getSurvey());

        //For each score add proper items
        for(CompositeScore compositeScore:compositeScoreList){
            //add score
            feedbackList.add(new CompositeScoreFeedback(compositeScore));

            //add its questions
            List<Question> questions=compositeScore.getQuestions();
            for(Question question:questions){
                Value valueInSurvey=question.getValueBySurvey(survey);
                feedbackList.add(new QuestionFeedback(valueInSurvey));
            }
        }
        return feedbackList;
    }

}
