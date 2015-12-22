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

import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.Date;

/**
 * Simple VO that wraps a survey in order to show it in the planned tab
 * Created by arrizabalaga on 15/12/15.
 */
public class PlannedSurvey implements PlannedItem {
    private final Survey survey;

    private final static String HIGH_PRODUCTIVITY="H";
    private final static String LOW_PRODUCTIVITY="L";
    private final static String NO_QUALITY_OF_CARE="-";

    /**
     * The header where this item belongs
     */
    private PlannedHeader header;

    public PlannedSurvey(Survey survey,PlannedHeader header) {
        this.survey = survey;
        this.header = header;
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
        if(survey==null){
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
        if(survey==null){
            return "";
        }

        return survey.getOrgUnit().isLowProductivity()?LOW_PRODUCTIVITY:HIGH_PRODUCTIVITY;
    }

    /**
     * Returns the mainscore as a String
     * @return
     */
    public String getQualityOfCare(){
        if(survey==null){
            return NO_QUALITY_OF_CARE;
        }
        return Utils.round(survey.getMainScore());
    }

    public Date getNextAssesment(){
        if(survey==null){
            return null;
        }
        return survey.getScheduledDate();
    }

    public Survey getSurvey(){
        return survey;
    }

    public PlannedHeader getHeader(){
        return header;
    }

    public void incHeaderCounter(){
        this.header.incCounter();
    }

    /**
     * Checks if this item can be shown according to the given filter
     * @param filterProgram
     * @return
     */
    @Override
    public boolean isShownByProgram(Program filterProgram){
        //No filter -> always show
        if(filterProgram==null){
            return true;
        }

        Program surveyProgram=survey.getTabGroup().getProgram();
        //Returns if both match
        return filterProgram.getId_program().equals(surveyProgram.getId_program());
    }

    /**
     * Headers are always shown
     * @param plannedHeader
     * @return
     */
    @Override
    public boolean isShownByHeader(PlannedHeader plannedHeader){
        //No filter -> always show
        if(plannedHeader==null){
            return true;
        }

        //Returns if both match
        return this.header.equals(plannedHeader);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlannedSurvey that = (PlannedSurvey) o;

        if (survey != null ? !survey.equals(that.survey) : that.survey != null) return false;
        return !(header != null ? !header.equals(that.header) : that.header != null);

    }

    @Override
    public int hashCode() {
        int result = survey != null ? survey.hashCode() : 0;
        result = 31 * result + (header != null ? header.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlannedSurvey{" +
                "survey=" + survey +
                '}';
    }
}
