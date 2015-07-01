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

package org.eyeseetea.malariacare.database.utils;

import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;

/**
 * VO that holds the completion ratio of answered/expected questions
 * Created by arrizabalaga on 1/07/15.
 */
public class SurveyAnsweredRatio {
    /**
     * Total number of questions to answer
     */
    private int total;

    /**
     * Total number of answered questions
     */
    private int answered;

    public SurveyAnsweredRatio(int total, int answered) {
        this.total = total;
        this.answered = answered;
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

}
