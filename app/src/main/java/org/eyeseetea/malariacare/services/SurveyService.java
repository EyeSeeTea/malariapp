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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Survey$Table;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Tab$Table;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.database.utils.feedback.FeedbackBuilder;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Utils;

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
     * Name of 'list unsent' action
     */
    public static final String ALL_UNSENT_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_UNSENT_SURVEYS_ACTION";

    /**
     * Name of 'list uncompleted' action
     */
    public static final String ALL_UNCOMPLETED_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_UNCOMPLETED_SURVEYS_ACTION";

    /**
     * Name of 'list completed' action
     */
    public static final String ALL_SENT_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_SENT_SURVEYS_ACTION";

    /**
     * Name of 'reload' action which returns both lists (unsent, sent)
     */
    public static final String RELOAD_DASHBOARD_ACTION ="org.eyeseetea.malariacare.services.SurveyService.RELOAD_DASHBOARD_ACTION";

    /**
     * Name of 'show' action
     */
    public static final String PREPARE_SURVEY_ACTION ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION";

    /**
     * Name of 'feedback' action
     */
    public static final String PREPARE_FEEDBACK_ACTION="org.eyeseetea.malariacare.services.SurveyService.PREPARE_FEEDBACK_ACTION";

    /**
     * Key of composite scores entry in shared session
     */
    public static final String PREPARE_SURVEY_ACTION_COMPOSITE_SCORES ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION_COMPOSITE_SCORES";

    /**
     * Key of tabs entry in shared session
     */
    public static final String PREPARE_SURVEY_ACTION_TABS ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION_TABS";

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
            case PREPARE_SURVEY_ACTION:
                prepareSurveyInfo();
                break;
            case ALL_UNSENT_SURVEYS_ACTION:
                getAllUnsentSurveys();
                break;
            case ALL_UNCOMPLETED_SURVEYS_ACTION:
                getAllUncompletedSurveys();
                break;
            case ALL_SENT_SURVEYS_ACTION:
                getAllSentSurveys();
                break;
            case RELOAD_DASHBOARD_ACTION:
                reloadDashboard();
                break;
            case PRELOAD_TAB_ITEMS:
                Log.e(".SurveyService", "Pre-loading tab: " + intent.getLongExtra("tab", 0));
                preLoadTabItems(intent.getLongExtra("tab", 0));
                break;
            case PREPARE_FEEDBACK_ACTION:
                getFeedbackItems();
                break;
        }
    }

    private void preLoadTabItems(Long tabID){
        List<Tab> tabs = new Select().from(Tab.class).where(Condition.column(Tab$Table.ID_TAB).eq(tabID)).queryList();
        if (tabs !=null && tabs.size()>=1)
            Utils.preloadTabItems(tabs.get(0));
    }

    private void reloadDashboard(){
        Log.d(TAG, "reloadDashboard");
        List<Survey> surveys=new Select().all().from(Survey.class)
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();

        List<Survey> unsentSurveys=new ArrayList<>();
        List<Survey> sentSurveys=new ArrayList<>();
        for(Survey survey:surveys){
            if(!survey.isSent()){
                unsentSurveys.add(survey);
                survey.getAnsweredQuestionRatio();
            }else{
                sentSurveys.add(survey);
            }
        }

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_UNSENT_SURVEYS_ACTION, unsentSurveys);
        Session.putServiceValue(ALL_SENT_SURVEYS_ACTION, sentSurveys);

        //Returning result to anyone listening
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ALL_UNSENT_SURVEYS_ACTION));
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ALL_SENT_SURVEYS_ACTION));
    }

    /**
     * Action that calculates the 'feedback' items corresponding to the current survey in session
     */
    private void getFeedbackItems(){
        //Mock some items
        List<Feedback> feedbackList= FeedbackBuilder.build(Session.getSurvey());

        //Return result to anyone listening
        Log.d(TAG, String.format("getFeedbackItems: %d", feedbackList.size()));

        Session.putServiceValue(PREPARE_FEEDBACK_ACTION_ITEMS, feedbackList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PREPARE_FEEDBACK_ACTION));
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
     * Selects all sent surveys from database
     */
    private void getAllSentSurveys(){
        Log.d(TAG,"getAllUnsentSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<Survey> surveys = Survey.getAllSentSurveys();

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_SENT_SURVEYS_ACTION,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_SENT_SURVEYS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void getAllUncompletedSurveys(){
        Log.d(TAG,"getAllUnsentSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<Survey> surveys = Survey.getAllUncompletedSurveys();

        //Load %completion in every survey (it takes a while so it can NOT be done in UI Thread)
        for(Survey survey:surveys){
            survey.getAnsweredQuestionRatio();
        }

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_UNCOMPLETED_SURVEYS_ACTION,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_UNCOMPLETED_SURVEYS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void getAllCompletedSurveys(){
        Log.d(TAG,"getAllUnsentSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<Survey> surveys = Survey.getAllCompletedSurveys();

        //Load %completion in every survey (it takes a while so it can NOT be done in UI Thread)
        for(Survey survey:surveys){
            survey.getAnsweredQuestionRatio();
        }

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_SENT_SURVEYS_ACTION,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_SENT_SURVEYS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    /**
     * Prepares required data to show a survey completely (tabs and composite scores).
     */
    private void prepareSurveyInfo(){
        Log.d(TAG, "prepareSurveyInfo (Thread:" + Thread.currentThread().getId() + ")");

//        Survey survey=Session.getSurvey();
//        Program program=survey.getProgram();

        //Get composite scores for current program & register them (scores)
        //List<CompositeScore> compositeScores = CompositeScore.listAllByProgram(program);
        if(Utils.isPictureQuestion()){
            Log.d(TAG, "prepareSurveyInfo (Thread:" + Thread.currentThread().getId() + ")");

            Survey survey=Session.getSurvey();
            Program program=survey.getProgram();

            //Get composite scores for current program & register them (scores)
            List<CompositeScore> compositeScores = CompositeScore.listAllByProgram(program);
            ScoreRegister.registerCompositeScores(compositeScores);

            //Get tabs for current program & register them (scores)
            List<Tab> tabs = Tab.getPictureTabsBySession();
            ScoreRegister.registerTabScores(tabs);

            //Since intents does NOT admit NON serializable as values we use Session instead
            Session.putServiceValue(PREPARE_SURVEY_ACTION_COMPOSITE_SCORES,compositeScores);
            Session.putServiceValue(PREPARE_SURVEY_ACTION_TABS,tabs);

            //Returning result to anyone listening
            Intent resultIntent= new Intent(PREPARE_SURVEY_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
        }
        else {
            List<CompositeScore> compositeScores = new Select().all().from(CompositeScore.class).queryList();
            ScoreRegister.registerCompositeScores(compositeScores);

            //Get tabs for current program & register them (scores)
            List<Tab> tabs = Tab.getTabsBySession();
            ScoreRegister.registerTabScores(tabs);

            //Since intents does NOT admit NON serializable as values we use Session instead
            Session.putServiceValue(PREPARE_SURVEY_ACTION_COMPOSITE_SCORES, compositeScores);
            Session.putServiceValue(PREPARE_SURVEY_ACTION_TABS, tabs);

            //Returning result to anyone listening
            Intent resultIntent = new Intent(PREPARE_SURVEY_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
        }
    }
}
