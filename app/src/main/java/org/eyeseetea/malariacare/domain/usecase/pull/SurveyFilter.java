/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.domain.usecase.pull;


import java.util.Date;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class SurveyFilter {

    Date startDate;
    Date endDate;
    Integer maxEvents;
    String programUId;
    String orgUnitUId;
    boolean isQuarantineSurvey;

    private SurveyFilter(Date startDate, Date endDate, Integer maxEvents, String programUId, String orgUnitUId, boolean isQuarantineSurvey) {
        validateDates(startDate, endDate);
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxEvents = maxEvents;
        this.programUId = programUId;
        this.isQuarantineSurvey = isQuarantineSurvey;
        this.orgUnitUId = orgUnitUId;
    }

    private void validateDates(Date startDate, Date endDate) {
        if(endDate == null || startDate == null){
            return;
        }
        if(endDate.after(startDate)){
            throw new IllegalArgumentException("Start date should be lower or equal than end Date");
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public Integer getMaxEvents() {
        return maxEvents;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getProgramUId() {
        return programUId;
    }

    public String getOrgUnitUId() {
        return orgUnitUId;
    }

    public boolean isQuarantineSurvey() {
        return isQuarantineSurvey;
    }

    public static SurveyFilter createCheckQuarantineOnServerFilter(Date startDate, Date endDate, String programUId, String orgunitUId){
        return new SurveyFilter(
                required(startDate, "startDate is required"),
                required(endDate, "endDate is required"),
                null,
                required(programUId, "programUId is required"),
                required(orgunitUId, "orgUnitUId is required"),
                true
                );
    }
    public static SurveyFilter createGetQuarantineSurveys(String programUId, String orgunitUId){
        return new SurveyFilter(
                null,
                null,
                null,
                required(programUId, "programUId is required"),
                required(orgunitUId, "orgUnitUId is required"),
                true
        );
    }
    public static SurveyFilter createGetSurveysOnPull(Date startDate, int maxEvents){
        return new SurveyFilter(
                required(startDate, "startDate is required"),
                null,
                maxEvents,
                null,
                null,
                false
        );
    }
}
