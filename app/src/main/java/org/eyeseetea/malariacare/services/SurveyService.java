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

import java.util.ArrayList;
import java.util.List;

/**
 * A service that looks for current Surveys to show on Dashboard(Details) in an asyn manner.
 * Created by arrizabalaga on 16/06/15.
 */
public class SurveyService extends IntentService {

    /**
     * The constant used to broadcast the result of the service
     */
    public static final String BROADCAST_SERVICE="org.eyeseetea.malariacare.services.SurveyService.BROADCAST";

    /**
     * The name of the parameter where the result of the service is returned
     */
    public static final String BROADCAST_RESULT="org.eyeseetea.malariacare.services.SurveyService.RESULT";

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
        //Select surveys from sql
        List<Survey> surveys = Survey.getAllUnsentSurveys();

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(BROADCAST_RESULT,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(BROADCAST_SERVICE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }
}
