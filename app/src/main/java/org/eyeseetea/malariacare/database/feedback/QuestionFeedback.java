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

package org.eyeseetea.malariacare.database.feedback;

import org.eyeseetea.malariacare.database.model.Value;

/**
 * Created by arrizabalaga on 14/09/15.
 */
public class QuestionFeedback implements Feedback {

    private Value value;

    public QuestionFeedback(Value value){
        this.value=value;
    }

    @Override
    public String getLabel() {
        return this.value.getQuestion().getForm_name();
    }

    @Override
    public boolean hasToHideByPassing() {
        //If the selected option gives 'points' -> 'hideable'
        return this.value.getOption().getFactor()>0;
    }

    /**
     * Returns the feedback info associated with this row
     * @return
     */
    public String getFeedback(){
        //TODO Add feedback column to question|option
        return "Something <b> important</b> needs to be done";
    }
}
