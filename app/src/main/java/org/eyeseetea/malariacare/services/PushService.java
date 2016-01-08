/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Facility QA Tool App.  If not, see <http://www.gnu.org/licenses/>.
 */


        package org.eyeseetea.malariacare.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.network.PushClient;
import org.eyeseetea.malariacare.network.PushResult;

import java.util.List;

/**
 * A service that runs pushing process for pending surveys.
 * Created by rhardjon on 19/09/15.
 */
public class PushService extends IntentService {

    /**
     * Constant added to the intent in order to reuse the service for different 'methods'
     */
    public static final String SERVICE_METHOD="serviceMethod";

    /**
     * Name of 'push all pending surveys' action
     */
    public static final String PENDING_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.PushService.PENDING_SURVEYS_ACTION";

    /**
     * Tag for logging
     */
    public static final String TAG = ".PushService";

    /**
     * Constructor required due to a error message in AndroidManifest.xml if it is not present
     */
    public PushService(){
        super(PushService.class.getSimpleName());
    }

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PushService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Take action to be done
        switch (intent.getStringExtra(SERVICE_METHOD)){
            case PENDING_SURVEYS_ACTION:
                pushAllPendingSurveys();
                break;
        }
    }

    /**
     * Push all pending surveys
     */
    private void pushAllPendingSurveys() {
        Log.d(TAG, "pushAllPendingSurveys (Thread:" + Thread.currentThread().getId() + ")");

        //Select surveys from sql
        List<Survey> surveys = Survey.getAllUnsentSurveys();

        if(surveys!=null && !surveys.isEmpty()){
            for(Survey survey : surveys){
                if(survey.isCompleted()){
                    sendPush(survey);
                }
            }
        }
    }

    private void sendPush (Survey survey) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    String user=sharedPreferences.getString(getString(R.string.dhis_user), "");
    String password=sharedPreferences.getString(getString(R.string.dhis_password),"");
    PushClient pushClient=new PushClient(survey, this.getApplicationContext(),user,password);

    //Push  data
    pushClient.pushBackground();
    }

}
