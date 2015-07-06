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
import android.util.Log;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;

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
     * Name of 'list' action
     */
    public static final String ALL_UNSENT_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_UNSENT_SURVEYS_ACTION";

    /**
     * Name of 'show' action
     */
    public static final String PREPARE_SURVEY_ACTION ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION";

    /**
     * Key of composite scores entry in shared session
     */
    public static final String PREPARE_SURVEY_ACTION_COMPOSITE_SCORES ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION_COMPOSITE_SCORES";

    /**
     * Key of tabs entry in shared session
     */
    public static final String PREPARE_SURVEY_ACTION_TABS ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION_TABS";

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
            case PREPARE_SURVEY_ACTION:
                prepareSurveyInfo();
                break;
            case ALL_UNSENT_SURVEYS_ACTION:
                getAllUnsentSurveys();
                break;
        }
    }

    /**
     * Selects all pending surveys from database
     */
    private void getAllUnsentSurveys(){
        Log.d(TAG,"getAllUnsentSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<Survey> surveys = Survey.getAllUnsentSurveys();

        //Load %completion in every survey (it takes a while so it can NOT be done in UI Thread)
        for(Survey survey:surveys){
            survey.getAnsweredQuestionRatio();
        }

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_UNSENT_SURVEYS_ACTION,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_UNSENT_SURVEYS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    /**
     * Prepares required data to show a survey completely (tabs and composite scores).
     */
    private void prepareSurveyInfo(){
        Log.d(TAG, "prepareSurveyInfo (Thread:" + Thread.currentThread().getId() + ")");

        Survey survey=Session.getSurvey();
        Program program=survey.getProgram();

        //Get composite scores for current program & register them (scores)
        //List<CompositeScore> compositeScores = CompositeScore.listAllByProgram(program);

        List<CompositeScore> compositeScores = CompositeScore.listAll(CompositeScore.class);
        ScoreRegister.registerCompositeScores(compositeScores);

        //Get tabs for current program & register them (scores)
        List<Tab> tabs = Tab.getTabsBySession();
        ScoreRegister.registerTabScores(tabs);

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(PREPARE_SURVEY_ACTION_COMPOSITE_SCORES,compositeScores);
        Session.putServiceValue(PREPARE_SURVEY_ACTION_TABS,tabs);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(PREPARE_SURVEY_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }
}
