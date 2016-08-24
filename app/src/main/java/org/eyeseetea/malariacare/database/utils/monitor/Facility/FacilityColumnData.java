/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.database.utils.monitor.facility;

import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.utils.AUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arrizabalaga on 13/10/15.
 */
public class FacilityColumnData {
    private List<Survey> surveys;

    public FacilityColumnData(){
        surveys=new ArrayList<>();
    }

    /**
     * Adds a survey to this cell
     * @param survey
     */
    public void addSurvey(Survey survey){
        surveys.add(survey);
    }

    /**
     * Returns the value of the columns formatted as javascript number or null if no surveys for this cell
     * @return
     */
    public String getAsJSON(){
        if(!hasSurveys()){
            return "null";
        }
        return AUtils.round(getValue());
    }

    /**
     * Calculates the value for this column (facility/month)
     * @return
     */
    private float getValue(){
        if(!hasSurveys()){
            return 0f;
        }
        float averageFacilityMonth=0;
        for(Survey survey:surveys){
            averageFacilityMonth+=survey.getMainScore();
        }
        return averageFacilityMonth/surveys.size();
    }

    private boolean hasSurveys(){
        return surveys!=null && surveys.size()>0;
    }
}
