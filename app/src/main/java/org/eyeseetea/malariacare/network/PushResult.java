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

package org.eyeseetea.malariacare.network;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arrizabalaga on 5/07/15.
 */
public class PushResult {
    public static final String UPDATED = "updated";
    public static final String IMPORTED = "imported";
    public static final String IGNORED = "ignored";
    public static final String DHIS220_RESPONSE = "response";

    private JSONObject jsonObject;
    private Exception exception;


    public PushResult(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public PushResult(Exception exception) {
        this.exception = exception;
    }

    public boolean isSuccessful(){
        return exception==null && jsonObject!=null;
    }

    public String getUpdated(){
        if(!isSuccessful()){
            return "0";
        }

        return getValue(UPDATED);
    }

    public String getIgnored(){
        if(!isSuccessful()){
            return "0";
        }

        return getValue(IGNORED);
    }

    public String getImported(){
        if(!isSuccessful()){
            return "0";
        }

        return getValue(IMPORTED);
    }

    public Exception getException(){
        return exception;
    }

    private String getValue(String key){
        try {
            //DHIS 2.19
            return jsonObject.getString(key);
        } catch (JSONException e){
        }

        try {
            //DHIS 2.20
            JSONObject response=jsonObject.getJSONObject(DHIS220_RESPONSE);
            return response.getString(key);
        } catch (JSONException e) {
            return "";
        }
    }


}
