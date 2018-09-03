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
    String orgunitUId;
    boolean isQuarantineSurvey;

    public SurveyFilter(Date startDate, Date endDate, Integer maxEvents, String programUId, String orgunitUid, boolean isQuarantineSurvey) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxEvents = maxEvents;
        this.programUId = programUId;
        this.isQuarantineSurvey = isQuarantineSurvey;
        this.orgunitUId = orgunitUid;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getProgramUId() {
        return programUId;
    }

    public String getOrgunitUId() {
        return orgunitUId;
    }

    public boolean isQuarantineSurvey() {
        return isQuarantineSurvey;
    }

    public static SurveyFilter createCheckOnServerFilter(Date startDate, Date endDate, String programUId, String orgunitUId){
        return new SurveyFilter(
                required(startDate, "startDate is required"),
                required(endDate, "endDate is required"),
                null,
                required(programUId, "programUId is required"),
                required(orgunitUId, "orgunitUId is required"),
                true
                );
    }
    public static SurveyFilter createGetQuarantineSurveys(String programUId, String orgunitUId){
        return new SurveyFilter(
                null,
                null,
                null,
                required(programUId, "programUId is required"),
                required(orgunitUId, "orgunitUId is required"),
                true
        );
    }
}
