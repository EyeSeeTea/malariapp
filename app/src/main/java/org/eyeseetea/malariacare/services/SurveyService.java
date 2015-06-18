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

package org.eyeseetea.malariacare.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;

import java.util.List;

/**
 * A service that looks for current Surveys to show on Dashboard(Details) in an asyn manner.
 * Created by arrizabalaga on 16/06/15.
 */
public class SurveyService extends IntentService {

    /**
     * Constant added to the intent in order to reuse the service for different 'methods'
     */
    public static final String SERVICE_METHOD="serviceMethod";

    /**
     * The constant used to broadcast the result of the service
     */
    public static final String ALL_UNSENT_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_UNSENT_SURVEYS_ACTION";

    /**
     * Constructor required due to a error message in AndroidManifest.xml if it is not present
     */
    public SurveyService(){
        super(SurveyService.class.getSimpleName());
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SurveyService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Take action to be done
        switch (intent.getStringExtra(SERVICE_METHOD)){
            //TODO all additional background methods
            case ALL_UNSENT_SURVEYS_ACTION:
                getAllUnsentSurveys();
                break;
        }
    }

    /**
     * Selects all pending surveys from database
     */
    protected void getAllUnsentSurveys(){
        //Select surveys from sql
        List<Survey> surveys = Survey.getAllUnsentSurveys();

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_UNSENT_SURVEYS_ACTION,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_UNSENT_SURVEYS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }
}
