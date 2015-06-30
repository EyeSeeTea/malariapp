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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.io.InputStream;


public abstract class BaseActivity extends ActionBarActivity {

    /**
     * Extra param to annotate the activity to return after settings
     */
    public static final String SETTINGS_CALLER_ACTIVITY = "SETTINGS_CALLER_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);

        initView(savedInstanceState);
    }

    /**
     * Common styling
     */
    private void initView(Bundle savedInstanceState){
        setTheme(R.style.EyeSeeTheme);
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);

        if (savedInstanceState == null){
            this.overridePendingTransition(R.transition.anim_slide_in_left,
                    R.transition.anim_slide_out_right);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_general, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                debugMessage("User asked for settings");
                goSettings();
                break;
            case R.id.action_license:
                debugMessage("User asked for license");
                showAlertWithMessage(R.string.settings_menu_licence, R.raw.gpl);
                break;
            case R.id.action_about:
                debugMessage("User asked for about");
                showAlertWithMessage(R.string.settings_menu_about, R.raw.about);
                break;
            case R.id.action_logout:
                debugMessage("User asked for logout");
                logout();
                break;
            case android.R.id.home:
                debugMessage("Go back");
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Every BaseActivity(Details, Create, Survey) goes back to DashBoard
     */
    public void onBackPressed(){
        finishAndGo(DashboardDetailsActivity.class);
    }

    @Override
    public void onResume(){
        super.onResume();
        Intent intent;
        intent = (getCallingActivity() != null) ? new Intent(getCallingActivity().getClassName()) : getIntent();

        if (intent.getStringExtra("activity") != null && getCallingActivity() != null && intent.getStringExtra("activity").equals("settings")){
            Log.i(".onResume", "coming from settings");
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();

            overridePendingTransition(0, 0);
            startActivity(intent);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void goSettings(){
        Intent intentSettings=new Intent(this,SettingsActivity.class);
        intentSettings.putExtra(SETTINGS_CALLER_ACTIVITY,this.getClass());
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Closes current session and goes back to loginactivity
     */
    protected void logout(){
        new AlertDialog.Builder(this)
                .setTitle(getApplicationContext().getString(R.string.settings_menu_logout))
                .setMessage(getApplicationContext().getString(R.string.dialog_content_logout_confirmation))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Session.logout();
                        finishAndGo(LoginActivity.class);
                    }
                })
                .setNegativeButton(android.R.string.no, null).create().show();
    }


    /**
     * Called when the user clicks the New Survey button
     */
    public void newSurvey(View view) {
        Intent targetActivityIntent = new Intent(this,CreateSurveyActivity.class);
        startActivity(targetActivityIntent);
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

    /**
     * Launches an activity with the given class
     * @param targetActivityClass Given target activity class
     */
    public void go(Class targetActivityClass){
        Intent targetActivityIntent = new Intent(this,targetActivityClass);
        startActivity(targetActivityIntent);
    }



    /**
     * Shows an alert dialog with a big message inside based on a raw resource
     * @param titleId Id of the title resource
     * @param rawId Id of the raw text resource
     */
    private void showAlertWithMessage(int titleId, int rawId){
        InputStream message = getApplicationContext().getResources().openRawResource(rawId);
        new AlertDialog.Builder(this)
                .setTitle(getApplicationContext().getString(titleId))
                .setMessage(Utils.convertFromInputStreamToString(message))
                .setNeutralButton(android.R.string.ok, null).create().show();
    }

    /**
     * Logs a debug message using current activity SimpleName as tag. Ex:
     *   SurveyActivity => ".SurveyActivity"
     * @param message
     */
    private void debugMessage(String message){
        Log.d("." + this.getClass().getSimpleName(), message);
    }

}
