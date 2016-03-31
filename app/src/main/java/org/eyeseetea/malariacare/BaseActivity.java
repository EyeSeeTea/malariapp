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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.listeners.SurveyLocationListener;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.utils.VariantSpecificUtils;
import org.hisp.dhis.android.sdk.controllers.DhisService;
import org.hisp.dhis.android.sdk.events.UiEvent;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;

import java.io.InputStream;


public abstract class BaseActivity extends ActionBarActivity {

    /**
     * Extra param to annotate the activity to return after settings
     */
    public static final String SETTINGS_CALLER_ACTIVITY = "SETTINGS_CALLER_ACTIVITY";

    private SurveyLocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        Dhis2Application.bus.register(this);
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
            initTransition();
        }
    }

    /**
     * Customize transitions for these activities
     */
    protected void initTransition(){
        this.overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_left);
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
                showAlertWithHtmlMessageAndLastCommit(R.string.settings_menu_about, R.raw.about);
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
        finishAndGo(DashboardActivity.class);
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
    public void onStop(){
        try {
            Dhis2Application.bus.unregister(this);
        }catch(Exception e){}
        super.onStop();
    }

    @Override
    public void onDestroy(){
        try {
            Dhis2Application.bus.unregister(this);
        }catch(Exception e){}
        super.onDestroy();
    }

    @Override
    public void onRestart(){
        try {
            Dhis2Application.bus.register(this);
        }catch(Exception e){}
        super.onRestart();
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
                        //Start logout
                        debugMessage("Logging out from sdk...");
                        PreferencesState.getInstance().clearOrgUnitPreference();
                        DhisService.logOutUser(BaseActivity.this);
                    }
                })
                .setNegativeButton(android.R.string.no, null).create().show();
    }

    /**
     * Asks for location (required while starting to edit a survey)
     * @param survey
     */
    public void prepareLocationListener(Survey survey){

        locationListener=new SurveyLocationListener(survey.getId_survey());
        LocationManager locationManager=(LocationManager) LocationMemory.getContext().getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            debugMessage("requestLocationUpdates via GPS");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }

        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            debugMessage("requestLocationUpdates via NETWORK");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        }else{
            Location lastLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            debugMessage("location not available via GPS|NETWORK, last know: " + lastLocation);
            locationListener.saveLocation(lastLocation);
        }
    }

    public void onLogoutFinished(UiEvent uiEvent){
        //No event or not a logout event -> done
        if(uiEvent==null || !uiEvent.getEventType().equals(UiEvent.UiEventType.USER_LOG_OUT)){
            return;
        }
        debugMessage("Logging out from sdk...OK");
        Session.logout();
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
        VariantSpecificUtils.showAlert(titleId, Utils.convertFromInputStreamToString(message).toString(), BaseActivity.this);
    }

    /**
     * Shows an alert dialog with a big message inside based on a raw resource HTML formatted
     * @param titleId Id of the title resource
     * @param rawId Id of the raw text resource in HTML format
     */
    private void showAlertWithHtmlMessageAndLastCommit(int titleId, int rawId){
        String stringMessage = getMessageWithCommit(rawId);
        final SpannableString linkedMessage = new SpannableString(Html.fromHtml(stringMessage));
        Linkify.addLinks(linkedMessage, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

        VariantSpecificUtils.showAlert(titleId, linkedMessage, BaseActivity.this);
    }

    /**
     * Merge the lastcommit into the raw file
     * @param rawId Id of the raw text resource in HTML format
     */
    private String getMessageWithCommit(int rawId) {
        InputStream message = getApplicationContext().getResources().openRawResource(rawId);
        String stringCommit;
        //Check if lastcommit.txt file exist, and if not exist show as unavailable.
        int layoutId = getApplicationContext().getResources().getIdentifier("lastcommit", "raw", getApplicationContext().getPackageName());
        if (layoutId == 0){
            stringCommit=getString(R.string.unavailable);
        } else {
            InputStream commit = getApplicationContext().getResources().openRawResource( layoutId);
            stringCommit=Utils.convertFromInputStreamToString(commit).toString();
        }
        String stringMessage=Utils.convertFromInputStreamToString(message).toString();
        if(stringCommit.contains(getString(R.string.unavailable))){
            stringCommit=String.format(getString(R.string.lastcommit),stringCommit);
            stringCommit=stringCommit+" "+getText(R.string.lastcommit_unavailable);
        }
        else {
            stringCommit = String.format(getString(R.string.lastcommit), stringCommit);
        }
        stringMessage=String.format(stringMessage,stringCommit);
        return stringMessage;
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
