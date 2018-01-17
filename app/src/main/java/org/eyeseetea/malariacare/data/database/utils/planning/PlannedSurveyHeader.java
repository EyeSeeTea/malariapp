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

package org.eyeseetea.malariacare.data.database.utils.planning;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.utils.AUtils;

import java.util.Date;

/**
 * Simple VO that wraps a survey in order to show it in the planned tab
 * Created by arrizabalaga on 15/12/15.
 */
public class PlannedSurveyHeader implements PlannedItem {
    final private PlannedHeader plannedHeader;

    PlannedSurveyHeader(PlannedHeader plannedHeader){
        this.plannedHeader = plannedHeader;
    }
    @Override
    public boolean isShownByProgram(ProgramDB filterProgram) {
        return true;
    }

    @Override
    public boolean isShownByHeader(PlannedHeader currentSection) {
        //No filter -> always show
        if(plannedHeader==null){
            return false;
        }

        //Returns if both match
        return this.plannedHeader.equals(currentSection);
    }
}
