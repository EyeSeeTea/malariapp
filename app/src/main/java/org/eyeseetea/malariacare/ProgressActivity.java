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
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullProgressStatus;
import org.hisp.dhis.android.sdk.job.NetworkJob;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;

public class ProgressActivity extends Activity {

    private static final String TAG=".ProgressActivity";

    ProgressBar progressBar;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        prepareUI();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.first_pull), getString(R.string.incompleted_pull));
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        Dhis2Application.bus.register(this);
        PullController.getInstance().pull(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Dhis2Application.bus.unregister(this);
    }

    private void prepareUI(){
        progressBar=(ProgressBar)findViewById(R.id.pull_progress);
        textView=(TextView)findViewById(R.id.pull_text);
    }

    @Subscribe
    public void onPullProgressChange(final PullProgressStatus pullProgressStatus) {
        if(pullProgressStatus==null){
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(pullProgressStatus.hasError()){
                    showStatus(pullProgressStatus.getException().getMessage());
                    return;
                }

                //Step
                if(pullProgressStatus.hasProgress()){
                    step(pullProgressStatus.getMessage());
                    return;
                }

                //Finish
                if(pullProgressStatus.isFinish()) {
                    showAndGoDashboard();
                }
            }
        });

    }

    /**
     * Shows a dialog with the given message
     * @param msg
     */
    private void showStatus(String msg){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_pull_response))
                .setMessage(msg)
                .setNeutralButton(android.R.string.yes,null).create().show();
    }

    /**
     * Prints the step in the progress bar
     * @param msg
     */
    private void step(final String msg) {
        //Error
        final int currentProgress = progressBar.getProgress();
        progressBar.setProgress(currentProgress + 1);
        textView.setText(msg);
    }

    /**
     * Shows a dialog to tell that pull is done and then moves into the dashboard
     */
    private void showAndGoDashboard() {
        step(getString(R.string.progress_pull_done));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.first_pull),getString(R.string.completed_pull));
        editor.commit();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_pull_response))
                .setMessage(R.string.dialog_pull_success)
                .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent i = new Intent(ProgressActivity.this, DashboardActivity.class);
                        startActivity(i);
                        finish();
                    }
                }).create().show();
    }


}
