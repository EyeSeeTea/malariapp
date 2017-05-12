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

package org.eyeseetea.malariacare.data.database.utils.monitor.allassessments;

import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.utils.AUtils;

import java.util.Date;

/**
 * Created by idelcano on 23/08/2016.
 */
public class EntrySentSurveysChartByOrgUnit  extends  EntrySentSurveysChartBase{
    public static final String JAVASCRIPT_ADD_DATA = "javascript:setOrgUnitData([%d, %d, '%s', '%s','%s'])";

    /**
     * The date whose month represents this point (day info is discarded)
     */
    OrgUnit orgUnit;
    public EntrySentSurveysChartByOrgUnit(int expected, Date date,OrgUnit orgUnit) {
        super(expected, date);
        this.orgUnit=orgUnit;
    }

    /**
     * Returns a javascript that be inyected into the webview
     * @return
     */
    public String getEntryAsJS(){
        return String.format(JAVASCRIPT_ADD_DATA,sent,expected, AUtils.escapeQuotes(orgUnit.getName()),orgUnit.getUid(),getDateAsString());
    }
}
