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

package org.eyeseetea.malariacare.database.utils.feedback;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;

/**
 * Created by arrizabalaga on 14/09/15.
 */
public class CompositeScoreFeedback implements Feedback {

    private CompositeScore compositeScore;

    public CompositeScoreFeedback(CompositeScore compositeScore){
        this.compositeScore=compositeScore;
    }

    @Override
    public String getLabel() {
        return this.compositeScore.getHierarchical_code()+" "+this.compositeScore.getLabel();
    }

    @Override
    public boolean isPassed() {
        return false;
    }

    /**
     * Returns the mark obtained for 'this' compositeScore
     * @return
     */
    public float getScore(float idSurvey) {
        return ScoreRegister.getCompositeScore(this.compositeScore,  idSurvey);
    }

    /**
     * Returns the mark obtained for 'this' compositeScore according to the current Survey in session
     * @return The percentage as a String
     */
    public String getPercentageAsString(float idSurvey){
        return String.format("%.1f %%", getScore(idSurvey));
    }

    /**
     * Returns the background color for this composite score row according to its hierarchy
     * @return
     */
    public int getBackgroundColor(){
        String code=this.compositeScore.getHierarchical_code();

        //Count number of '.' in string
        int numDots = code.length() - code.replace(".", "").length();

        if(numDots==0){
            return R.color.feedbackDarkBlue;
        }

        if(numDots==1){
            return R.color.feedbackLightBlue;
        }

        return R.color.scoreGrandson;
    }

    @Override
    public int hashCode() {
        return this.compositeScore.hashCode();
    }
}
