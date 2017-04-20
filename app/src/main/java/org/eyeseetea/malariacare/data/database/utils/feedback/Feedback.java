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

package org.eyeseetea.malariacare.data.database.utils.feedback;

/**
 * Base class for every feedback row:
 *  - CompositeScoreDB
 *  - Question
 *
 * Created by arrizabalaga on 14/09/15.
 */
public interface Feedback {

    /**
     * Returns the Label to show in the row
     * @return
     */
    String getLabel();

    /**
     * Returns if 'this' row has to hide according to its value
     * @return true|false
     */
    boolean isPassed();
}
