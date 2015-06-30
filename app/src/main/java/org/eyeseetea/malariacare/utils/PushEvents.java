/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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

package org.eyeseetea.malariacare.utils;

import android.util.Log;

import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Jose on 20/06/2015.
 */
public class PushEvents {

    Survey survey;

    public PushEvents(Survey survey) {
        this.survey = survey;
    }

    public void run () {

        JSONObject object = new JSONObject();

        try {

            //METADATA
            object.put("program", survey.getProgram().getUid());
            object.put("orgUnit", survey.getOrgUnit().getUid());
            object.put("eventDate", android.text.format.DateFormat.format("yyyy-MM-dd", survey.getEventDate()));
            object.put("status", "COMPLETED");
            object.put("storedBy", survey.getUser().getName());

            //QUESTIONS
            JSONArray values = new JSONArray();

            for (Value value : survey.getValues()) {
                JSONObject itemArray = new JSONObject();

                itemArray.put("dataElement", value.getQuestion().getUid());

                if (value.getValue() == null)
                    itemArray.put("value", value.getOption().getName());
                else
                    itemArray.put("value", value.getValue());

                values.put(itemArray);
            }

            object.put("dataValues", values);

            //SCORES


            Log.d(".PushEvents", object.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }

        int i;

        i=2;

    }

}
