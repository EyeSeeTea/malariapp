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
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.data.database.utils.feedback.FeedbackBuilder;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
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
     * Name of 'feedback' action
     */
    public static final String PREPARE_FEEDBACK_ACTION="org.eyeseetea.malariacare.services.SurveyService.PREPARE_FEEDBACK_ACTION";

    /**
     * Key of
     */
    public static final String PRELOAD_TAB_ITEMS ="org.eyeseetea.malariacare.services.SurveyService.PRELOAD_TAB_ITEMS";

    /**
     * Key of 'feedback' items in shared session
     */
    public static final String PREPARE_FEEDBACK_ACTION_ITEMS="org.eyeseetea.malariacare.services.SurveyService.PREPARE_FEEDBACK_ACTION_ITEMS";

    /**
     * Tag for logging
     */
    public static final String TAG = ".SurveyService";

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
            case PRELOAD_TAB_ITEMS:
                Log.i(".SurveyService", "Pre-loading tab: " + intent.getLongExtra("tab", 0));
                Log.i(".SurveyService", "Active module: " + intent.getStringExtra(Constants.MODULE_KEY));
                preLoadTabItems(intent.getLongExtra("tab", 0),intent.getStringExtra(Constants.MODULE_KEY));
                break;
            case PREPARE_FEEDBACK_ACTION:
                Log.i(".SurveyService", "Active module: " + intent.getStringExtra(Constants.MODULE_KEY));
                getFeedbackItems(intent.getStringExtra(Constants.MODULE_KEY));
                break;
        }
    }

    private void preLoadTabItems(Long tabID, String module){
        TabDB tab = TabDB.findById(tabID);
        if (tab !=null) {
            AUtils.preloadTabItems(tab, module);
        }
    }

    /**
     * Action that calculates the 'feedback' items corresponding to the current survey in session
     */
    private void getFeedbackItems(String module){
        List<Feedback> feedbackList= new ArrayList<>();
        Session.putServiceValue(PREPARE_FEEDBACK_ACTION_ITEMS, feedbackList);

        feedbackList= FeedbackBuilder.build(Session.getSurveyByModule(module), module);

        //Return result to anyone listening
        Log.d(TAG, String.format("getFeedbackItems: %d", feedbackList.size()));

        Session.putServiceValue(PREPARE_FEEDBACK_ACTION_ITEMS, feedbackList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PREPARE_FEEDBACK_ACTION));
    }
}
