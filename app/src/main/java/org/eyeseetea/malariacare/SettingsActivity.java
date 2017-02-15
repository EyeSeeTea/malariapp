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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.hisp.dhis.android.sdk.controllers.DhisService;
import org.hisp.dhis.android.sdk.events.UiEvent;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    private static final String TAG=".SettingsActivity";

    protected void onCreate(Bundle savedInstanceState) {
        //Register into sdk bug for listening to askIfLogout events
        Dhis2Application.bus.register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop(){
        try {
            //Unregister from bus before leaving
            Dhis2Application.bus.unregister(this);
        }catch(Exception e){}
        super.onStop();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Logging out from sdk is an async method.
     * Thus it is required a callback to finish askIfLogout gracefully.
     *
     * @param uiEvent
     */
    @Subscribe
    public void onLogoutFinished(UiEvent uiEvent){
        //No event or not a askIfLogout event -> done
        if(uiEvent==null || !uiEvent.getEventType().equals(UiEvent.UiEventType.USER_LOG_OUT)){
            return;
        }
        Log.i(TAG, "Logging out from sdk...OK");
        Session.logout();
        Intent loginIntent = new Intent(this,LoginActivity.class);
        finish();
        startActivity(loginIntent);
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // fitler the font options by screen size
        filterTextSizeOptions(findPreference(getApplicationContext().getString(R.string.font_sizes)));

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(getApplicationContext().getString(R.string.font_sizes)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.dhis_max_items)));

        Preference serverUrlPreference = (Preference)findPreference(getResources().getString(R.string.dhis_url));
        Preference userPreference = (Preference)findPreference(getResources().getString(R.string.dhis_user));
        Preference passwordPreference = (Preference)findPreference(getResources().getString(R.string.dhis_password));

        //Hide developer option if is not active in the json
        if(!AppSettingsBuilder.isDeveloperOptionsActive())
            getPreferenceScreen().removePreference(getPreferenceScreen().findPreference(getResources().getString(R.string.developer_option)));

        bindPreferenceSummaryToValue(serverUrlPreference);
        bindPreferenceSummaryToValue(userPreference);

        serverUrlPreference.setOnPreferenceClickListener(new LoginRequiredOnPreferenceClickListener(this));
        userPreference.setOnPreferenceClickListener(new LoginRequiredOnPreferenceClickListener(this));
        passwordPreference.setOnPreferenceClickListener(new LoginRequiredOnPreferenceClickListener(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    //Remove the last text size option if the screen size is small.
    private static void filterTextSizeOptions(Preference preference) {
        if(!PreferencesState.getInstance().isLargeTextShown()) {
            ListPreference listPreference = (ListPreference) preference;
            CharSequence[] entries = removeLastItem(listPreference.getEntries());
            CharSequence[] values = removeLastItem(listPreference.getEntryValues());
            listPreference.setEntries(entries);
            listPreference.setEntryValues(values);
        }
    }

    //Returns the provided charSequence without the last position.
    private static CharSequence[] removeLastItem(CharSequence[] entries) {
        CharSequence[] newEntries=new CharSequence[4];
        for(int i=0;i<entries.length-1;i++){
            newEntries[i]=entries[i];
        }
        return newEntries;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
         preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.font_sizes)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.dhis_url)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.dhis_max_items)));

            Preference serverUrlPreference = (Preference)findPreference(getResources().getString(R.string.dhis_url));
            Preference userPreference = (Preference)findPreference(getResources().getString(R.string.dhis_user));
            Preference passwordPreference = (Preference)findPreference(getResources().getString(R.string.dhis_password));

            bindPreferenceSummaryToValue(serverUrlPreference);
            bindPreferenceSummaryToValue(userPreference);

            SettingsActivity settingsActivity = (SettingsActivity)getActivity();
            serverUrlPreference.setOnPreferenceClickListener(new LoginRequiredOnPreferenceClickListener(settingsActivity));
            userPreference.setOnPreferenceClickListener(new LoginRequiredOnPreferenceClickListener(settingsActivity));
            passwordPreference.setOnPreferenceClickListener(new LoginRequiredOnPreferenceClickListener(settingsActivity));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean isValidFragment(String fragment){
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        //Reload changes into PreferencesState
        PreferencesState.getInstance().reloadPreferences();
    }

    @Override
    public void onBackPressed() {
        Class callerActivityClass=getCallerActivity();
        Intent returnIntent=new Intent(this,callerActivityClass);
        startActivity(returnIntent);
    }

    private Class getCallerActivity(){
        //FIXME Not working as it should the intent param is always null
        Intent creationIntent=getIntent();
        if(creationIntent==null){
            return DashboardActivity.class;
        }
        Class callerActivity=(Class)creationIntent.getSerializableExtra(BaseActivity.SETTINGS_CALLER_ACTIVITY);
        if(callerActivity==null){
            return DashboardActivity.class;
        }

        return callerActivity;
    }

}

/**
 * Listener that moves to the LoginActivity before changing DHIS config
 */
class LoginRequiredOnPreferenceClickListener implements Preference.OnPreferenceClickListener{

    private static final String TAG="LoginPreferenceListener";

    /**
     * Reference to the activity so you can use this from the activity or the fragment
     */
    SettingsActivity activity;

    LoginRequiredOnPreferenceClickListener(SettingsActivity activity){
        this.activity=activity;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.settings_menu_logout))
                .setMessage(activity.getString(R.string.dialog_content_dhis_preference_login))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //finish activity and go to login
                        Log.i(TAG, "Logging out from sdk...");
                        DhisService.logOutUser(activity);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
        Log.i(TAG, "Returning from dialog -> true");
        return true;
    }
}
