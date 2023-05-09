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

package org.eyeseetea.malariacare.data.database.utils.monitor.facilities;

import org.eyeseetea.malariacare.domain.entity.Survey;

import java.util.ArrayList;
import java.util.List;

public class FacilityColumnCounterData {
    private List<Survey> surveys;

    public FacilityColumnCounterData() {
        surveys = new ArrayList<>();
    }

    public void addSurvey(Survey survey) {
        surveys.add(survey);
    }


    public String getAsJSON() {
        if (!hasSurveys()) {
            return "0";
        }
        return surveys.size() + "";
    }

    private boolean hasSurveys() {
        return surveys != null && surveys.size() > 0;
    }
}
