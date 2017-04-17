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

package org.eyeseetea.malariacare.data.database.utils.multikeydictionaries;

import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyEntity;

import java.util.List;

/**
 * Created by idelcano on 19/09/2016.
 */
public class ProgramOUSurveyDict extends ObjectEntityDict {

    @Override
    public void put(String programUid, String orgUnitUid, Object survey) {
        super.put(programUid, orgUnitUid, survey);
    }

    @Override
    public SurveyEntity get(String programUid, String orgUnitUid) {
        return (SurveyEntity) super.get(programUid, orgUnitUid);
    }

    @Override
    public List<SurveyEntity> values() {
        return (List<SurveyEntity>) super.values();
    }
}
