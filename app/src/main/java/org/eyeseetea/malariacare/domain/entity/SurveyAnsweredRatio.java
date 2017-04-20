/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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

package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatioDB;

/**
 * VO that holds the completion ratio of answered/expected questions
 * Created by arrizabalaga on 1/07/15.
 */
public class SurveyAnsweredRatio {
    /**
     * Id of the related survey
     */
    private long surveyId;
    /**
     * Total number of questions to answer
     */
    private int total;

    /**
     * Total number of answered questions
     */
    private int answered;

    /**
     * Total number of compulsoryAnswered questions
     */
    private int compulsoryAnswered;
    /**
     * Total number of compulsoryAnswered questions
     */
    private int totalCompulsory;



    public SurveyAnsweredRatio(long surveyId, int total, int answered, int totalCompulsory, int compulsoryAnswered) {
        this.surveyId = surveyId;
        this.total = total;
        this.answered = answered;
        this.totalCompulsory=totalCompulsory;
        this.compulsoryAnswered = compulsoryAnswered;
    }

    public static SurveyAnsweredRatio getModelToEntity(long id_survey){
        SurveyAnsweredRatioDB surveyAnsweredRatioDB = SurveyAnsweredRatioDB.getSurveyAnsweredRatioBySurveyId(id_survey);
        if(surveyAnsweredRatioDB ==null) {
            return null;
        }
        return  new SurveyAnsweredRatio(surveyAnsweredRatioDB.getIdSurvey(),
                surveyAnsweredRatioDB.getTotalQuestions(),
                surveyAnsweredRatioDB.getAnsweredQuestions(), surveyAnsweredRatioDB.getTotalCompulsoryQuestions(), surveyAnsweredRatioDB.getAnsweredCompulsoryQuestions());
    }

    public long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(long surveyId) {
        this.surveyId = surveyId;
    }

    public int getAnswered() {
        return answered;
    }

    public void setAnswered(int answered) {
        this.answered = answered;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalCompulsory() {
        return totalCompulsory;
    }

    public void setTotalCompulsory(int totalCompulsory) {
        this.totalCompulsory = totalCompulsory;
    }

    public int getCompulsoryAnswered() {
        return compulsoryAnswered;
    }

    public void setCompulsoryAnswered(int compulsoryAnswered) {
        this.compulsoryAnswered = compulsoryAnswered;
    }
    /**
     * Return the ratio of completion
     * @return answered/total
     */
    public float getRatio(){
        if (total==0){
            //Not correct from a math perspective but most practical approach
            return 0;
        }

        if(isCompleted()){
            return 1;
        }

        return (float)answered/total;
    }
    /**
     * Return the ratio of completion compulsoryAnswered
     * @return answered/total
     */
    public float getCompulsoryRatio(){
        if (totalCompulsory==0){
            //Not correct from a math perspective but most practical approach
            return 0;
        }

        if(isCompleted()){
            return 1;
        }

        return (float) compulsoryAnswered /totalCompulsory;
    }
    /**
     * Checks if the related survey is completed or not.
     * If there are NO questions it returns false.
     * @return true|false
     */
    public boolean isCompleted(){
        if(total<=0){
            return false;
        }

        return answered>=total;
    }

    /**
     * Checks if the related survey has every compulsory question completed.
     * @return
     */
    public boolean isCompulsoryCompleted(){
        //No compulsory -> ok
        if(totalCompulsory==0){
            return true;
        }

        return compulsoryAnswered>=totalCompulsory;
    }

}
