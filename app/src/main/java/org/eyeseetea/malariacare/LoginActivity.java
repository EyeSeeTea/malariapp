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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.internal.Util;
import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.PopulatePictureAppDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.utils.Utils;
import org.hisp.dhis.android.sdk.controllers.DhisService;
import org.hisp.dhis.android.sdk.job.NetworkJob;
import org.hisp.dhis.android.sdk.network.Credentials;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Login Screen.
 * It shows only when the user has an open session.
 */
public class LoginActivity extends org.hisp.dhis.android.sdk.ui.activities.LoginActivity implements LoaderCallbacks<Cursor> {

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
        super.onCreate(savedInstanceState);
        if (User.getLoggedUser() != null) {
            startActivity(new Intent(LoginActivity.this,
                    ((Dhis2Application) getApplication()).getMainActivity()));
            finish();
        }

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

    @Override
    public void login(String serverUrl, String username, String password) {
        //This method is overriden to capture credentials data
        this.serverUrl=serverUrl;
        this.username=username;
        this.password=password;

        //Delegate real login attempt to parent
        super.login(serverUrl,username,password);
    }

    @Subscribe
    public void onLoginFinished(NetworkJob.NetworkJobResult<ResourceType> result) {
        if(result!=null && result.getResourceType().equals(ResourceType.USERS)) {
            if(result.getResponseHolder().getApiException() == null) {
                saveUserDetails();
                //FIXME remove when create survey is fixed
//                populateFromAssets();
                populatePictureAppFromAssets();
                launchMainActivity();
            } else {
                onLoginFail(result.getResponseHolder().getApiException());
            }
        }
    }

    /**
     * Utility method to use while developing to avoid a real pull
     */
    private void populateFromAssets() {
        try{
            User user = new User();
            user.save();
            Session.setUser(user);
            PullController.getInstance().wipeDatabase();
            PopulateDB.populateDB(getAssets());
        }catch(Exception ex){

        }
    }

    /**
     * Utility method to pictureApp Build
     */
    private void populatePictureAppFromAssets() {
        try{
            PullController.getInstance().wipeDatabase();
            if(Utils.isPictureQuestion())
            PopulatePictureAppDB.populateDB(getAssets());
            else
            PopulateDB.populateDB(getAssets());
            User user = new User();
            user.save();
            Session.setUser(user);
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

}



