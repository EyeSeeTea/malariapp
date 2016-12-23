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

package org.eyeseetea.malariacare.database.utils.multikeydictionaries;

import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.model.Answer;

import java.util.List;

/**
 * Created by idelcano on 19/09/2016.
 */
public class ProgramAnswerDict extends ObjectModelDict {
    @Override
    public void put(String programUid, String optionSetUid, BaseModel answer) {
        super.put(programUid, optionSetUid, answer);

    }

    @Override
    public Answer get(String programUid, String optionSetUid) {
        return (Answer) super.get(programUid, optionSetUid);
    }

    @Override
    public List<Answer> values() {
        return (List<Answer>) super.values();
    }
}
