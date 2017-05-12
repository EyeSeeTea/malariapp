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

package org.eyeseetea.malariacare.data.database.utils.services;

import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 08/08/2016.
 */
public class PlannedServiceBundle extends BaseServiceBundle {
    private List<PlannedItem> plannedItems = new ArrayList<>();

    public List<PlannedItem> getPlannedItems(){return plannedItems;}

    public void setPlannedItems(List<PlannedItem> plannedItems){this.plannedItems=plannedItems;}
}
