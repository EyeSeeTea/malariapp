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

/**
 * Just a marker interface to add headers and real items to a listview
 * Created by arrizabalaga on 15/12/15.
 */
public interface PlannedItem {

    /**
     * Tells if this item can be shown according to the given program as a filter
     * @param filterProgram
     * @return
     */
    boolean isShownByProgram(ProgramDB filterProgram);

    /**
     * Tells if this item can be shown according to the given header as a filter
     * @param currentSection
     * @return
     */
    boolean isShownByHeader(PlannedHeader currentSection);
}
