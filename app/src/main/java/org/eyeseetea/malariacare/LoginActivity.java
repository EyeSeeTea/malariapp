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
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Permissions;
import org.hisp.dhis.android.sdk.job.NetworkJob;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;

import java.io.InputStream;

/**
 * Login Screen.
 * It shows only when the user has an open session.
 */
public class LoginActivity extends org.hisp.dhis.android.sdk.ui.activities.LoginActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG="LoginActivity";
    /**
     * DHIS server URL
     */
    private String serverUrl;

    /**
     * DHIS username account
     */
    private String username;

    /**
     * DHIS password (required since push is done natively instead of using sdk)
     */
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        PreferencesState.getInstance().loadsLanguageInActivity();
        requestPermissions();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (User.getLoggedUser() != null && !ProgressActivity.PULL_CANCEL &&  sharedPreferences.getBoolean(getApplicationContext().getResources().getString(R.string.pull_metadata),false)) {
            startActivity(new Intent(LoginActivity.this,
                    ((Dhis2Application) getApplication()).getMainActivity()));
            finish();
        }
        ProgressActivity.PULL_CANCEL =false;
        EditText serverText = (EditText) findViewById(org.hisp.dhis.android.sdk.R.id.server_url);
        serverText.setText(R.string.login_info_dhis_default_server_url);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Shows an alert dialog asking for acceptance of the EULA terms. If ok calls login function, do nothing otherwise
     * @param titleId
     * @param rawId
     * @param context
     */
    public void askEula(int titleId, int rawId, final Context context){
        InputStream message = context.getResources().openRawResource(rawId);
        String stringMessage = AUtils.convertFromInputStreamToString(message).toString();
        final SpannableString linkedMessage = new SpannableString(Html.fromHtml(stringMessage));
        Linkify.addLinks(linkedMessage, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(titleId))
                .setMessage(linkedMessage)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rememberEulaAccepted(context);
                        loginToDhis(serverUrl,username,password);
                    }
                })
                .setNegativeButton(android.R.string.no, null).create();
        dialog.show();
        ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Save a preference to remember that EULA was already accepted
     * @param context
     */
    public void rememberEulaAccepted(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.eula_accepted), true);
        editor.commit();
    }

    /**
     * User SDK function to login
     * @param serverUrl
     * @param username
     * @param password
     */
    public void loginToDhis(String serverUrl, String username, String password){
        //Delegate real login attempt to parent in sdk
        super.login(serverUrl, username, password);
    }

    /**
     * Ask for EULA acceptance if this is the first time user login to the server, otherwise login
     * @param serverUrl
     * @param username
     * @param password
     */
    @Override
    public void login(String serverUrl, String username, String password) {
        //This method is overriden to capture credentials data
        this.serverUrl=serverUrl;
        this.username=username;
        this.password=password;


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(getString(R.string.eula_accepted), false)) {
            askEula(R.string.settings_menu_eula, R.raw.eula, LoginActivity.this);
        } else {
            loginToDhis(serverUrl, username, password);
        }
    }

    @Subscribe
    public void onLoginFinished(NetworkJob.NetworkJobResult<ResourceType> result) {
        if(result!=null && result.getResourceType().equals(ResourceType.USERS)) {
            if(result.getResponseHolder().getApiException() == null) {
                saveUserDetails();

                populateFromAssetsIfRequired();

                launchMainActivity();
            } else {
                onLoginFail(result.getResponseHolder().getApiException());
            }
        }
    }

    /**
     * Utility method to use while developing to avoid a real pull
     */
    private void populateFromAssetsIfRequired() {
        //From server -> done
        if(PreferencesState.getInstance().getPullFromServer()) {
            return;
        }

        //Populate locally
        try{
            PullController.getInstance().wipeDatabase();
            User user = new User();
            user.save();
            Session.setUser(user);
            PopulateDB.populateDB(getAssets());
        }catch(Exception ex){
        }
    }

    /**
     * Saves user credentials into preferences
     */
    private void saveUserDetails(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.dhis_url), this.serverUrl);
        editor.putString(getString(R.string.dhis_user), this.username);
        editor.putString(getString(R.string.dhis_password), this.password);
        editor.commit();
    }

    /**
     * LoginActivity does NOT admin going backwads since it is always the first activity.
     * Thus onBackPressed closes the app
     */
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void requestPermissions() {
        if (EyeSeeTeaApplication.permissions == null) {
            EyeSeeTeaApplication.permissions = Permissions.getInstance(this);
        }
        if (!EyeSeeTeaApplication.permissions.areAllPermissionsGranted()) {
            EyeSeeTeaApplication.permissions.requestNextPermission();
        }
    }

    /**
     * Its called on the requestPermission results, if the user accepts the permissions it request
     * the Phone permission and gets the phoneMetadata
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        if (Permissions.processAnswer(requestCode, permissions, grantResults)) {
            EyeSeeTeaApplication.permissions.requestNextPermission();
        } else if (EyeSeeTeaApplication.permissions.hasNextPermission()) {
            EyeSeeTeaApplication.permissions.requestNextPermission();
        }
    }
}



