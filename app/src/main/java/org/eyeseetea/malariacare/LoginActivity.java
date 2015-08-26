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
import android.app.Activity;
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

import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Login Screen.
 * It shows only when the user has an open session.
 */
public class LoginActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private AutoCompleteTextView mServerUrlView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(".LoginActivity", "onCreate");
        super.onCreate(savedInstanceState);

        //Show form
        initView();
    }

    /**
     * Initialize login form and listeners
     */
    private void initView(){
        setContentView(R.layout.login_layout);
        // Set up the login form.
        mUserView = (AutoCompleteTextView) findViewById(R.id.user);
        mServerUrlView = (AutoCompleteTextView) findViewById(R.id.dhis_url);
        populateAutoComplete();

        // In case the user set previously a different DHIS2 server URL or user in the settings, this is filled in automatically.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String urlInPreferences = settings.getString(getApplicationContext().getString(R.string.dhis_url), "");
        if (!urlInPreferences.equals("")){
            mServerUrlView.setText(urlInPreferences);
        }
        String userInPreferences = settings.getString(getApplicationContext().getString(R.string.dhis_user), "");
        if (!userInPreferences.equals("")){
            mUserView.setText(userInPreferences);
        }

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUserSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        Log.i(".LoginActivity", "attempt!!");

        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        if(!hasGoodCredentials(user,password)){
            mUserView.requestFocus();
            mUserView.setError(getString(R.string.login_error_bad_credentials));
            return;
        }

        showProgress(true);
        mAuthTask = new UserLoginTask(user);
        mAuthTask.execute((Void) null);
    }

    /**
     * Login form is cleaned when the activity is back to foreground
     */
    @Override
    public void onResume(){
        super.onResume();
    }

    /**
     * Checks if the pair user/password matches any dummy credentials.
     * @param user
     * @param password
     * @return
     */
    private boolean hasGoodCredentials(String user, String password){
        /*
        Introduce here the API call to check the credentials in case of API calls strategy
         */
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /***************************
     * FIXME: Not required so far
     ***************************/
    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUserView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private User user;

        UserLoginTask(String user) {
            mUser = user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                // restablish user with the data entered
                initUser();
                setDhisUserPreference();
                setDhisServerPreference();
            }catch(Exception ex) {
                Log.e(".LoginActivity", "Error doInBackground login", ex);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            stopProgress();
            //something went wrong
            if(!success){
                mUserView.requestFocus();
                mUserView.setError(getString(R.string.login_error_bad_credentials));
                return;
            }
            //finishAndGo dashboard
            Log.i(".LoginActivity", "Logged in!");
            // Set the user in the session
            Session.setUser(user);
            // return back to the calling activity the survey position in the dashboard and the ok returncode
            Intent resultData = new Intent();
            resultData.putExtra("Survey", getIntent().getIntExtra("Survey", 0));
            resultData.putExtra("User", mUserView.getText().toString());
            resultData.putExtra("Password", mPasswordView.getText().toString());
            setResult(Activity.RESULT_OK, resultData);
            finish();
        }

        @Override
        protected void onCancelled() {
            stopProgress();
        }

        /**
         * Add user to table and session
         */
        private void initUser(){
            // In case no user was previously set in the database we create one. Otherwise we update it
            List<User> users = new Select().all().from(User.class).queryList();
            if (users.size() == 0) {
                this.user = new User(mUser, mUser);
                this.user.save();
            } else {
                this.user = users.get(0);
                this.user.setName(mUser);
                this.user.setUid(mUser);
                this.user.update();
            }
        }

        /**
         * Fill in the preference given by the preference key with the text contained in the given view
         * @param view Component containing the text
         * @param preferenceKey resource that points to the String in strings.xml which is the preference key for the desired preference
         */
        private void setPreference(TextView view, int preferenceKey){
            String text = view.getText().toString();
            if (!text.equals("")) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(getApplicationContext().getString(preferenceKey), text);
                editor.commit();
            }
        }

        /**
         * Fill in the dhis user preference with what user selected in the login field
         */
        private void setDhisUserPreference(){
            setPreference(mUserView, R.string.dhis_user);
        }

        /**
         * Fill in the dhis server preference with what user selected in the login field
         */
        private void setDhisServerPreference(){
            setPreference(mServerUrlView, R.string.dhis_url);
        }

        /**
         * Stops task and progress spinner
         */
        private void stopProgress(){
            mAuthTask = null;
            showProgress(false);
        }
    }
}



