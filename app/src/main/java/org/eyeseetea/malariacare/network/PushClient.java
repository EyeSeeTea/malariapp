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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.PushController;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose on 20/06/2015.
 */
public class PushClient {

    private static final String TAG=".PushClient";
    /**
     * For pushing just 1 survey
     */
    Survey survey;

    /**
     * For pushing N surveys
     */
    List<Survey> surveys;

    Context applicationContext;
    NetworkUtils networkUtils;

    private PushClient(Context applicationContext, String user, String password) {
        this.applicationContext = applicationContext;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        networkUtils=new NetworkUtils(applicationContext);
        networkUtils.setDhisServer(sharedPreferences.getString(applicationContext.getResources().getString(R.string.dhis_url),""));
        networkUtils.setUser(user);
        networkUtils.setPassword(password);
    }

    public PushClient(List<Survey> surveys, Context applicationContext, String user, String password) {
        this(applicationContext,user,password);
        this.surveys = surveys;
    }

    public PushClient(Survey survey, Context applicationContext, String user, String password) {
        this(applicationContext,user,password);
        this.survey = survey;
    }

    public void pushSDK() {
        //No network -> Done
        if (!AUtils.isNetworkAvailable()) {
            AlarmPushReceiver.isDone();
        }
        //Push via sdk
        PushController.getInstance().push(PreferencesState.getInstance().getContext(), surveys);
    }

    public PushResult pushAPI() {
        if (AUtils.isNetworkAvailable()) {
               return malariaApiPush();
        }
        return new PushResult();
    }

    public PushResult malariaApiPush() {
        PushResult pushResult;
        try{
            //TODO: This should be removed once DHIS bug is solved
            //Map<String, JSONObject> controlData = prepareControlData();
            survey.prepareSurveyUploadedDate();
            JSONObject data = QueryFormatterUtils.getInstance().prepareMetadata(survey);
            //TODO: This should be removed once DHIS bug is solved
            //data = PushUtilsElements(data, controlData.get(""));
            data = QueryFormatterUtils.getInstance().PushUtilsElements(data, survey, Constants.PUSH_MODULE_KEY);
            pushResult = new PushResult(networkUtils.pushData(data));
            if(pushResult.isSuccessful() && !pushResult.getImported().equals("0")){
                //TODO: This should be removed once DHIS bug is solved
                //pushControlDataElements(controlData);
                survey.setSentSurveyState();
                AlarmPushReceiver.setFail(false);
            }
            else{
                AlarmPushReceiver.setFail(true);
            }
        }catch(Exception ex){
            AlarmPushReceiver.setFail(true);
            Log.e(TAG, ex.getMessage());
            pushResult=new PushResult(ex);
        }
        finally {
            //Success or not the dashboard must be reloaded
            DashboardActivity.reloadDashboard();
        }
        return  pushResult;
    }

}
