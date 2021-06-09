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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevelDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.data.database.utils.feedback.FeedbackBuilder;
import org.eyeseetea.malariacare.data.database.utils.services.BaseServiceBundle;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
     * Name of the parameter that holds every survey and filters that goes into the feedback
     */
    public static final String RELOAD_SENT_FRAGMENT_ACTION ="org.eyeseetea.malariacare.services.SurveyService.RELOAD_SENT_FRAGMENT_ACTION";

    /**
     * Name of 'list unsent or uncompleted' action
     */
    public static final String ALL_IN_PROGRESS_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION";
    /**
     * Name of 'list completed (and unsent)' action
     */
    public static final String ALL_COMPLETED_SURVEYS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_COMPLETED_SURVEYS_ACTION";

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
     * Name of 'All monitor data' action
     */
    public static final String ALL_MONITOR_DATA_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_MONITOR_DATA_ACTION";

    /**
     * Name of 'All create survey data' action
     */
    public static final String ALL_CREATE_SURVEY_DATA_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_CREATE_SURVEY_DATA_ACTION";
    /**
     * Name of 'All programs' action
     */
    public static final String ALL_PROGRAMS_ACTION ="org.eyeseetea.malariacare.services.SurveyService.ALL_PROGRAMS_ACTION";

    /**
     * Key of composite scores entry in shared session
     */
    public static final String PREPARE_SURVEY_ACTION_COMPOSITE_SCORES ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION_COMPOSITE_SCORES";

    /**
     * Key of tabs entry in shared session
     */
    public static final String PREPARE_SURVEY_ACTION_TABS ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_SURVEY_ACTION_TABS";

    /**
     * Key of tabs entry in shared session
     */
    public static final String PREPARE_ALL_TABS ="org.eyeseetea.malariacare.services.SurveyService.PREPARE_ALL_TABS";
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
                Log.i(".SurveyService", "Active module: " + intent.getStringExtra(Constants.MODULE_KEY));
                prepareSurveyInfo(intent.getStringExtra(Constants.MODULE_KEY));
                break;
            case ALL_IN_PROGRESS_SURVEYS_ACTION:
                getAllInProgressSurveys();
                break;
            case RELOAD_SENT_FRAGMENT_ACTION:
                reloadSentFragment();
                break;
            case ALL_COMPLETED_SURVEYS_ACTION:
                getAllCompletedSurveys();
                break;
            case RELOAD_DASHBOARD_ACTION:
                reloadDashboard();
                break;
            case PRELOAD_TAB_ITEMS:
                Log.i(".SurveyService", "Pre-loading tab: " + intent.getLongExtra("tab", 0));
                Log.i(".SurveyService", "Active module: " + intent.getStringExtra(Constants.MODULE_KEY));
                preLoadTabItems(intent.getLongExtra("tab", 0),intent.getStringExtra(Constants.MODULE_KEY));
                break;
            case PREPARE_FEEDBACK_ACTION:
                Log.i(".SurveyService", "Active module: " + intent.getStringExtra(Constants.MODULE_KEY));
                getFeedbackItems(intent.getStringExtra(Constants.MODULE_KEY));
                break;
            case ALL_MONITOR_DATA_ACTION:
                getAllMonitorData();
                break;
            case ALL_CREATE_SURVEY_DATA_ACTION:
                getAllCreateSurveyData();
                break;
            case ALL_PROGRAMS_ACTION:
                getAllPrograms();
                break;
        }
    }

    private void reloadSentFragment() {
        BaseServiceBundle sentDashboardBundle = new BaseServiceBundle();

        Log.d(TAG,"getAllSentCompletedOrConflictSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<SurveyDB> sentSurveyList;
        if(PreferencesState.getInstance().isLastForOrgUnit()) {
            sentSurveyList = SurveyDB.getLastSentCompletedOrConflictSurveys();
        }else{
            sentSurveyList = SurveyDB.getAllSentCompletedOrConflictSurveys();
        }
        sentDashboardBundle.addModelList(SurveyDB.class.getName(),sentSurveyList);
        sentDashboardBundle.addModelList(OrgUnitDB.class.getName(),OrgUnitDB.getAllOrgUnit());
        sentDashboardBundle.addModelList(ProgramDB.class.getName(),ProgramDB.getAllPrograms());

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(RELOAD_SENT_FRAGMENT_ACTION, sentDashboardBundle);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(RELOAD_SENT_FRAGMENT_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void reloadOnlyLastSentFragment() {
        BaseServiceBundle sentDashboardBundle = new BaseServiceBundle();

        Log.d(TAG,"getAllSentCompletedOrConflictSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<SurveyDB> sentSurveyList;

        sentSurveyList = SurveyDB.getLastSentCompletedOrConflictSurveys();
        sentDashboardBundle.addModelList(SurveyDB.class.getName(),sentSurveyList);
        sentDashboardBundle.addModelList(OrgUnitDB.class.getName(),OrgUnitDB.getAllOrgUnit());
        sentDashboardBundle.addModelList(ProgramDB.class.getName(),ProgramDB.getAllPrograms());

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(RELOAD_SENT_FRAGMENT_ACTION, sentDashboardBundle);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(RELOAD_SENT_FRAGMENT_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);

    }

    private void reloadPlannedSurveys() {
        Intent surveysIntent = new Intent(
                PreferencesState.getInstance().getContext().getApplicationContext(),
                PlannedSurveyService.class);
        surveysIntent.putExtra(PlannedSurveyService.SERVICE_METHOD,
                PlannedSurveyService.PLANNED_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
        surveysIntent = new Intent(
                PreferencesState.getInstance().getContext().getApplicationContext(),
                PlannedSurveyService.class);
        surveysIntent.putExtra(PlannedSurveyService.SERVICE_METHOD,
                PlannedSurveyService.PLANNED_PER_ORG_UNIT_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
    }

    private void getAllCreateSurveyData() {
        Log.d(TAG,"getAllCreateSurveyData (Thread:"+Thread.currentThread().getId()+")");

        BaseServiceBundle orgCreateSurveyData=new BaseServiceBundle();
        orgCreateSurveyData.addModelList(OrgUnitDB.class.getName(), OrgUnitDB.list());
        orgCreateSurveyData.addModelList(OrgUnitLevelDB.class.getName(), OrgUnitLevelDB.list());
        orgCreateSurveyData.addModelList(ProgramDB.class.getName(), ProgramDB.list());

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_CREATE_SURVEY_DATA_ACTION, orgCreateSurveyData);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_CREATE_SURVEY_DATA_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void getAllMonitorData() {
        Log.d(TAG,"getAllMonitorData (Thread:"+Thread.currentThread().getId()+")");
        List<ProgramDB> programList= ProgramDB.getAllPrograms();
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -5);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        List<SurveyDB> sentSurveys = SurveyDB.getAllSentCompletedOrConflictSurveysAfterDate(
                cal.getTime());
        List<OrgUnitDB> orgUnits= OrgUnitDB.list();

        BaseServiceBundle monitorMap=new BaseServiceBundle();
        monitorMap.addModelList(SurveyDB.class.getName(),sentSurveys);
        monitorMap.addModelList(ProgramDB.class.getName(),programList);
        monitorMap.addModelList(OrgUnitDB.class.getName(),orgUnits);
        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_MONITOR_DATA_ACTION, monitorMap);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_MONITOR_DATA_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void getAllPrograms() {
        Log.d(TAG,"getAllPrograms (Thread:"+Thread.currentThread().getId()+")");
        List<ProgramDB> programList= ProgramDB.getAllPrograms();
        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_PROGRAMS_ACTION, programList);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_PROGRAMS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void getAllInProgressSurveys() {
        Log.d(TAG,"getAllUncompletedUnsentUnplanedSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<SurveyDB> surveys = SurveyDB.getAllInProgressSurveys();

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_IN_PROGRESS_SURVEYS_ACTION,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_IN_PROGRESS_SURVEYS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private void preLoadTabItems(Long tabID, String module){
        TabDB tab = TabDB.findById(tabID);
        if (tab !=null) {
            AUtils.preloadTabItems(tab, module);
        }
    }

    private void reloadDashboard(){
        Log.d(TAG, "reloadDashboard");
        reloadPlannedSurveys();
        reloadSentFragment();
        getAllCompletedSurveys();
        getAllCreateSurveyData();
        getAllInProgressSurveys();
        getAllMonitorData();
        getAllPrograms();
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


    private void getAllCompletedSurveys(){
        Log.d(TAG,"getCompletedSurveys (Thread:"+Thread.currentThread().getId()+")");

        //Select surveys from sql
        List<SurveyDB> surveys = SurveyDB.getAllCompletedSurveys();

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(ALL_COMPLETED_SURVEYS_ACTION,surveys);

        //Returning result to anyone listening
        Intent resultIntent= new Intent(ALL_COMPLETED_SURVEYS_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
        //getAllSentDashboardData();
    }

    /**
     * Prepares required data to show a survey completely (tabs and composite scores).
     */
    private void prepareSurveyInfo(String module){
        Log.d(TAG, "prepareSurveyInfo (Thread:" + Thread.currentThread().getId() + ")");

        //register composite scores for current survey and module
        List<CompositeScoreDB> compositeScores = CompositeScoreDB.list();
        SurveyDB survey = Session.getSurveyByModule(module);
        ScoreRegister.registerCompositeScores(compositeScores,survey.getId_survey(),module);

        //Get tabs for current program & register them (scores)
        List<TabDB> tabs = TabDB.getTabsBySession(module);
        //old List<Tab> allTabs = new Select().all().from(Tab.class).where(Condition.column(Tab$Table.ID_PROGRAM).eq(survey.getProgram().getId_program())).queryList();
        List<TabDB> allTabs = TabDB.getAllTabsByProgram(survey.getProgram().getId_program());
        //register tabs scores for current survey and module
        ScoreRegister.registerTabScores(tabs, survey.getId_survey(), module);

        //Since intents does NOT admit NON serializable as values we use Session instead
        Session.putServiceValue(PREPARE_SURVEY_ACTION_COMPOSITE_SCORES, compositeScores);
        Session.putServiceValue(PREPARE_SURVEY_ACTION_TABS, tabs);
        Session.putServiceValue(PREPARE_ALL_TABS, allTabs);

        //Returning result to anyone listening
        Intent resultIntent = new Intent(PREPARE_SURVEY_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }
}
