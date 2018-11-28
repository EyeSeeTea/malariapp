/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.data.remote.api;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.SurveyFilter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SurveyAPIDataSource extends OkHttpClientDataSource implements ISurveyDataSource {

    private static final String TAG = ".SurveyAPIDataSource";

    public SurveyAPIDataSource(Credentials credentials) {
        super(credentials);
    }

    @Override
    public List<Survey> getSurveys(SurveyFilter filter) throws Exception {
        //Here pull surveys code
        List<Survey> surveys = new ArrayList<>();
        if(filter.isQuarantineSurvey()) {
            surveys = getEventsByUidFromServer(filter.getUids());
        }
        return surveys;
    }

    @Override
    public void save(List<Survey> surveys) throws Exception {
        //Here push surveys code
    }

    private List<Survey> pullQuarantineEvents(String url) throws IOException, JSONException {
        String response = executeCall(url);
        JSONObject events = new JSONObject(response);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(mapper.readTree(events.toString()),
                JsonNode.class);
        return existOnServerFromJson(jsonNode);
    }

    /**
     * Returns a list of surveys with the correct creation date ( setted from trackedentitydatavalue value)
     */
    private List<Survey> getEventsByUidFromServer(List<String> uids) throws IOException, JSONException {
        String url = ApiMapper.getFilteredEventPath(uids);
        Log.d(TAG, url);
        return pullQuarantineEvents(url);
    }


    private static List<Survey> existOnServerFromJson(JsonNode jsonNode) {
        return ApiMapper.mapSurveysFromJson(jsonNode);
    }
}
