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

package org.eyeseetea.malariacare.database.utils.monitor.Pie;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.utils.Constants;

/**
 * VO thats hold the info of a single pie chart
 * Created by arrizabalaga on 9/10/15.
 */
public class PieTabGroupDataBase {

    static final String JSONFORMAT="{title:'%s',tip:'%s',idTabGroup: %d,valueA:%d,valueB:%d,valueC:%d,uidprogram:'%s',uidorgunit:'%s'}";

    /**
     * Number of surveys with A score
     */
    int numA;
    /**
     * Number of surveys with B score
     */
    int numB;
    /**
     * Number of surveys with C score
     */
    int numC;


    /**
     * Increments the right counter according to the score
     * @param score
     */
    public void incCounter(float score){
        if(score< Constants.MAX_RED){
            numC++;
            return;
        }

        if(score< Constants.MAX_AMBER){
            numB++;
            return;
        }

        numA++;
        return;
    }



}
