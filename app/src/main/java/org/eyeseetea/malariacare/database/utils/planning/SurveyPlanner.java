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

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Survey$Table;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Helper that creates a 'next' planned survey from a given survey or from a orgUnit + program
 * Created by arrizabalaga on 16/12/15.
 */
public class SurveyPlanner {

    private final static int TYPE_A_NEXT_DATE=6;
    private final static int TYPE_BC_LOW_NEXT_DATE=4;
    private final static int TYPE_BC_HIGH_NEXT_DATE=2;


    /**
     * Builds a 'NEVER' planned survey for the given combination
     * @param orgUnit
     * @param program
     * @return
     */
    public Survey buildNext(OrgUnit orgUnit,Program program){
        Survey survey = new Survey();
        survey.setStatus(Constants.SURVEY_PLANNED);
        survey.setOrgUnit(orgUnit);
        survey.setUser(Session.getUser());

        List<TabGroup> tabGroups = program.getTabGroups();
        survey.setTabGroup(tabGroups.get(0));
        survey.save();

        return survey;
    }

    /**
     * Plans a new survey according to the given sent survey and its values
     * @param survey
     * @return
     */
    public Survey buildNext(Survey survey){
        Survey plannedSurvey = new Survey();
        plannedSurvey.setStatus(Constants.SURVEY_PLANNED);
        plannedSurvey.setOrgUnit(survey.getOrgUnit());
        plannedSurvey.setUser(Session.getUser());
        plannedSurvey.setTabGroup(survey.getTabGroup());
        plannedSurvey.setScheduledDate(findScheduledDateBySurvey(survey));
        plannedSurvey.save();

        return plannedSurvey;
    }

    /**
     * Starts a planned survey with the given orgUnit and tabGroup
     * @param orgUnit
     * @param tabGroup
     * @return
     */
    public Survey startSurvey(OrgUnit orgUnit,TabGroup tabGroup){
        //Find planned survey
        Survey survey = new Select()
                .from(Survey.class)
                .where(Condition.column(Survey$Table.ID_ORG_UNIT).eq(orgUnit.getId_org_unit()))
                .and(Condition.column(Survey$Table.ID_TAB_GROUP).eq(tabGroup.getId_tab_group()))
                .and(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_PLANNED))
                .querySingle();

        //There MUST be an already planned survey for this combo
        survey.setCreationDate(new Date());
        survey.setStatus(Constants.SURVEY_IN_PROGRESS);
        survey.setUser(Session.getUser());
        survey.save();

        //Reset mainscore for this 'real' survey
        survey.setMainScore(0f);
        survey.saveMainScore();
        return survey;
    }

    /**
     * Plans a new survey according to the last surveys that has been sent for each combo orgunit + program
     */
    public void buildNext(){

        //Select last sent survey for each combination
        List<Survey> surveys = new Select()
                .from(Survey.class)
                .where()
                .groupBy(new QueryBuilder().appendQuotedArray(Survey$Table.ID_ORG_UNIT, Survey$Table.ID_TAB_GROUP))
                .having(Condition.columnsWithFunction("max", "eventDate"))
                .queryList();

        //Plan a copy according to that survey
        for(Survey survey:surveys){
            buildNext(survey);
        }

    }

    private Date findScheduledDateBySurvey(Survey survey) {
        if(survey==null){
            return null;
        }

        Date scheduledDate=survey.getScheduledDate();
        if(scheduledDate==null){
            return null;
        }

        //A -> 6 months
        if(survey.isTypeA()){
            return getInXMonths(scheduledDate,TYPE_A_NEXT_DATE);
        }

        //BC + Low OrgUnit -> 4
        if(survey.getOrgUnit().isLowProductivity()){
            return getInXMonths(scheduledDate,TYPE_BC_LOW_NEXT_DATE);
        }

        //BC + High OrgUnit -> 2
        return getInXMonths(scheduledDate,TYPE_BC_HIGH_NEXT_DATE);
    }

    /**
     * Returns +30 days from the given date
     * @return
     */
    private Date getInXMonths(Date date,int numMonths){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH,numMonths);
        return calendar.getTime();
    }

}
