/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.PushController;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.SyncProgressStatus;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.hisp.dhis.android.sdk.controllers.DhisService;
import org.hisp.dhis.android.sdk.events.UiEvent;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;

import java.util.ArrayList;
import java.util.List;

public class ProgressActivity extends Activity {

    private static final String TAG=".ProgressActivity";

    /**
     * Intent param that tells what to do (push, pull or push before pull)
     */
    public static final String TYPE_OF_ACTION="TYPE_OF_ACTION";

    /**
     * To pull data from server
     */
    public static final int ACTION_PULL=0;

    /**
     * To push a single survey to server
     */
    public static final int ACTION_PUSH=1;

    /**
     * To push every unsent data to server before pulling metadata
     */
    public static final int ACTION_PUSH_BEFORE_PULL=2;

    /**
     * Num of expected steps while pulling
     */
    private static final int MAX_PULL_STEPS=7;

    /**
     * Num of expected steps while pushing
     */
    private static final int MAX_PUSH_STEPS=4;

    ProgressBar progressBar;
    TextView textView;

    boolean pullAfterPushInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        prepareUI();
        annotateFirstPull(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Dhis2Application.bus.register(this);
        launchAction();
    }

    @Override
    public void onPause() {
        super.onPause();
        Dhis2Application.bus.unregister(this);
    }

    private void prepareUI(){
        progressBar=(ProgressBar)findViewById(R.id.pull_progress);
        progressBar.setMax(isAPush() ? MAX_PUSH_STEPS : MAX_PULL_STEPS);
        textView=(TextView)findViewById(R.id.pull_text);
    }

    @Subscribe
    public void onProgressChange(final SyncProgressStatus syncProgressStatus) {
        if(syncProgressStatus ==null){
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (syncProgressStatus.hasError()) {
                    showException(syncProgressStatus.getException().getMessage());
                    return;
                }

                //Step
                if (syncProgressStatus.hasProgress()) {
                    step(syncProgressStatus.getMessage());
                    return;
                }

                //Finish
                if (syncProgressStatus.isFinish()) {
                    showAndMoveOn();
                }
            }
        });
    }

    /**
     * Launches a pull or push according to an intent extra
     */
    private void launchAction(){

        //Clear flag
        pullAfterPushInProgress=false;

        //Push or Pull according to extra param from intent
        if(isAPush()){
            launchPush();
        }else{
            launchPull();
        }
    }

    /**
     * Shows a dialog with the given message y move to login after showing error
     * @param msg
     */
    private void showException(String msg){
        final boolean isAPush=isAPush();
        String title=getDialogTitle(isAPush);

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(msg)
                .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //A crash during a push might be recoverable -> dashboard
                        if (isAPush) {
                            Log.d(TAG, "Push crashed, moving to dashboard...");
                            finishAndGo(DashboardActivity.class);
                        } else {
                            //A crash during a pull requires to start from scratch -> logout
                            Log.d(TAG, "Logging out from sdk...");
                            DhisService.logOutUser(ProgressActivity.this);
                        }
                    }
                })
                .create()
                .show();
    }

    /**
     * Prints the step in the progress bar
     * @param msg
     */
    private void step(final String msg) {
        final int currentProgress = progressBar.getProgress();
        progressBar.setProgress(currentProgress + 1);
        textView.setText(msg);
    }

    /**
     * Shows a dialog to tell that pull is done and then moves into the dashboard.
     *
     */
    private void showAndMoveOn() {
        boolean isAPush=isAPush();
        String title=getDialogTitle(isAPush);

        final int msg=getDoneMessage();

        //Show final step -> done
        step(getString(R.string.progress_pull_done));

        //Annotate pull is done
        if(!isAPush) {
            annotateFirstPull(true);
        }

        //Show message and go on -> pull or single push = dashboard | push before pull = start pull
        new AlertDialog.Builder(this)
				.setCancelable(false)
                .setTitle(title)
                .setMessage(msg)
                .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        //Pull or Push(single)
                        if (msg == R.string.dialog_pull_success || msg ==R.string.dialog_push_success) {
                            finishAndGo(DashboardActivity.class);
                            return;
                        }

                        //Start pull after push
                        pullAfterPushInProgress=true;
                        launchPull();
                    }
                }).create().show();
    }

    /**
     * Once an action is over there is a message that changes depending on the kind of action:
     *  -Pull: Pull ok, let's move to dashboard
     *  -Push (single): Push ok, let's move to dashboard
     *  -Push (before pull): Push ok, let's start with the pull
     *
     * @return
     */
    private int getDoneMessage(){
        boolean isAPush=isAPush();

        //Pull
        if(!isAPush){
            return R.string.dialog_pull_success;
        }

        //Push before pull
        if(hasAPullAfterPush()){
            return R.string.dialog_push_before_pull_success;
        }

        //Push (single)
        return R.string.dialog_push_success;
    }

    private void annotateFirstPull(boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pull_metadata), value);
        editor.commit();
    }

    /**
     * Tells if a push is required
     * @return
     */
    private boolean isAPush() {
        //A push before pull
        if(pullAfterPushInProgress){
            return false;
        }

        //Check intent params
        Intent i=getIntent();
        //Not a pull -> is a Push
        return (i!=null && i.getIntExtra(TYPE_OF_ACTION,ACTION_PULL)!=ACTION_PULL);
    }

    /**
     * Tells is the intent requires a Pull after the push is done
     * @return
     */
    private boolean hasAPullAfterPush(){
        Intent i=getIntent();
        return (i!=null && i.getIntExtra(TYPE_OF_ACTION,ACTION_PULL)==ACTION_PUSH_BEFORE_PULL);
    }

    private String getDialogTitle(boolean isAPush){
        int stringId=isAPush?R.string.dialog_title_push_response:R.string.dialog_title_pull_response;
        return getString(stringId);
    }

    private void launchPull(){
        progressBar.setProgress(0);
        progressBar.setMax(MAX_PULL_STEPS);
        PullController.getInstance().pull(this);
    }

    /**
     * Launches a push using the PushController according to the intent params
     */
    private void launchPush(){
        progressBar.setProgress(0);
        progressBar.setMax(MAX_PUSH_STEPS);

        List<Survey> surveys=findSurveysToPush();
        PushController.getInstance().push(this, surveys);
    }

    /**
     * Find the surveys that are going to be pushed
     * @return
     */
    private List<Survey> findSurveysToPush(){
        if(hasAPullAfterPush()){
            return Survey.getAllUnsentSurveys();
        }

        List<Survey> surveys=new ArrayList<>();
        surveys.add(Session.getSurvey());
        return surveys;
    }

    @Subscribe
    public void onLogoutFinished(UiEvent uiEvent){
        //No event or not a logout event -> done
        if(uiEvent==null || !uiEvent.getEventType().equals(UiEvent.UiEventType.USER_LOG_OUT)){
            return;
        }
        Log.d(TAG,"Logging out from sdk...OK");
        Session.logout();
        //Go to login
        finishAndGo(LoginActivity.class);
    }

    /**
     * Finish current activity and launches an activity with the given class
     * @param targetActivityClass Given target activity class
     */
    public void finishAndGo(Class targetActivityClass){
        Intent targetActivityIntent = new Intent(this,targetActivityClass);
        finish();
        startActivity(targetActivityIntent);
    }

}
