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

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.utils.AUtils;

import java.util.ArrayList;
import java.util.List;

public class CompositeScoreFeedback implements Feedback {

    private float score;
    private CompositeScoreDB compositeScore;
    private boolean isActive;
    private List<QuestionFeedback> mFeedbackList;
    private List<CompositeScoreFeedback> mCompositeScoreFeedbackList;
    private boolean isShown;
    private boolean isRoot;

    public boolean isShown() {
        return this.isShown;
    }

    public boolean isRoot() {
        return this.isRoot;
    }

    public CompositeScoreFeedback(CompositeScoreDB compositeScore, float score, boolean isRoot){
        this.compositeScore=compositeScore;
        this.score=score;
        mCompositeScoreFeedbackList = new ArrayList<>();
        mFeedbackList = new ArrayList<>();
        this.isShown = isRoot;
        this.isRoot = isRoot;
    }

    @Override
    public String getLabel() {
        return this.compositeScore.getHierarchical_code()+" "+this.compositeScore.getLabel();
    }

    @Override
    public boolean isPassed() {
        return false;
    }

    @Override
    public boolean hasMedia() {
        return true;
    }

    /**
     * Returns the mark obtained for 'this' compositeScore
     * @return
     */
    public float getScore(float idSurvey, String module) {
        return score;
    }

    /**
     * Returns the mark obtained for 'this' compositeScore according to the current Survey in session
     * @return The percentage as a String
     */
    public String getPercentageAsString(float idSurvey, String module){
        return AUtils.round(getScore(idSurvey, module),2);
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<QuestionFeedback> getFeedbackList() {
        return mFeedbackList;
    }

    public void setFeedbackList(
            List<QuestionFeedback> feedbackList) {
        mFeedbackList = feedbackList;
    }
    public List<CompositeScoreFeedback> getCompositeScoreFeedbackList() {
        return mCompositeScoreFeedbackList;
    }

    public void addCompositeScoreFeedbackList(CompositeScoreFeedback compositeScoreFeedback) {
        mCompositeScoreFeedbackList.add(compositeScoreFeedback);
    }
    @Override
    public int hashCode() {
        return this.compositeScore.hashCode();
    }

    public void toggleChildrenShown(boolean forceHide) {
        for(QuestionFeedback questionFeedback : getFeedbackList()){
            if(forceHide){
                questionFeedback.setShown(false);
            }else {
                questionFeedback.toggleShown();
            }
        }

        for(CompositeScoreFeedback compositeScoreFeedback : getCompositeScoreFeedbackList()){
            if(forceHide){
                compositeScoreFeedback.setShown(false);
            }else {
                compositeScoreFeedback.toggleShown();
            }
            if(!compositeScoreFeedback.isShown()){
                compositeScoreFeedback.toggleChildrenShown(true);
            }
        }
    }

    public void toggleShown() {
        if(isRoot()){
            return;
        }
        this.isShown = !this.isShown;
    }

    public void setShown(boolean value) {
        isShown=value;
    }
}
