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

package org.eyeseetea.malariacare.database.utils.planning;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Survey;

/**
 * Simple VO that wraps a survey in order to show it in the planned tab
 * Created by arrizabalaga on 15/12/15.
 */
public class PlannedSurvey implements PlannedItem {
    private final Survey survey;

    private final static String HIGH_PRODUCTIVITY="H";
    private final static String LOW_PRODUCTIVITY="L";
    private final static String NO_QUALITY_OF_CARE="-";

    public PlannedSurvey(Survey survey) {
        this.survey = survey;
    }

    public String getOrgUnit(){
        if(survey==null || survey.getOrgUnit()==null){
            return null;
        }
        if(survey.getOrgUnit()==null){
            return null;
        }
        return survey.getOrgUnit().getName();
    }

    public String getProgram(){
        if(survey==null || survey.getTabGroup()==null || survey.get){
            return null;
        }
        if(survey.getTabGroup()==null){
            return null;
        }
        if(survey.getTabGroup().getProgram()==null){
            return null;
        }
        return survey.getTabGroup().getProgram().getName();
    }

    /**
     * Returns a mocked productivity
     * @return
     */
    public String getProductivity(){
        return Math.random()>0.5?HIGH_PRODUCTIVITY:LOW_PRODUCTIVITY;
    }

    /**
     * Returns the mainscore as a String
     * @return
     */
    public String getQualityOfCare(){
        if(survey==null){
            return NO_QUALITY_OF_CARE;
        }

        return survey.getMainScore().toString();
    }

    public String getNextAssesment(){
        if(survey==null){
            return null;
        }
        //TODO merge dates here to return scheduledDates
        return null;
    }
}
