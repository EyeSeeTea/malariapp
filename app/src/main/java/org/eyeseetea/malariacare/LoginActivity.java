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

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.sdk.SdkLoginController;
import org.eyeseetea.malariacare.utils.AUtils;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;
import org.hisp.dhis.client.sdk.models.user.UserAccount;
import org.hisp.dhis.client.sdk.ui.activities.AbsLoginActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Login Screen.
 * It shows only when the user has an open session.
 */
public class LoginActivity extends AbsLoginActivity {
    private AlertDialog alertDialog;
    private static final String TAG = "LoginActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (User.getLoggedUser() != null && !ProgressActivity.PULL_CANCEL
                && sharedPreferences.getBoolean(
                getApplicationContext().getResources().getString(R.string.pull_metadata), false)) {
            launchActivity(LoginActivity.this, DashboardActivity.class);
        }
        ProgressActivity.PULL_CANCEL = false;
        getServerUrl().setText(R.string.login_info_dhis_default_server_url);
    }

    private void launchActivity(Activity activity, Class<?> activityClass) {
        Intent intent = new Intent(activity, activityClass);
        ActivityCompat.startActivity(LoginActivity.this, intent, null);
    }

    /**
     * Ask for EULA acceptance if this is the first time user login to the server, otherwise login
     */
    @Override
    protected void onLoginButtonClicked(Editable server, Editable username, Editable password) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(getString(R.string.eula_accepted), false)) {
            askEula(R.string.settings_menu_eula, R.raw.eula, LoginActivity.this);
        } else {
            loginToDhis(getServerUrl(), getUsername(), getPassword());
        }
    }

    @Override
    protected void onLogoutButtonClicked() {
        PreferencesState.getInstance().clearOrgUnitPreference();
        SdkLoginController.logOutUser();
    }

    /**
     * Shows an alert dialog asking for acceptance of the EULA terms. If ok calls login function,
     * do
     * nothing otherwise
     */
    public void askEula(int titleId, int rawId, final Context context) {
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
                        loginToDhis(getServerUrl(), getUsername(), getPassword());
                    }
                })
                .setNegativeButton(android.R.string.no, null).create();
        dialog.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(
                LinkMovementMethod.getInstance());
    }

    /**
     * Save a preference to remember that EULA was already accepted
     */
    public void rememberEulaAccepted(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.eula_accepted), true);
        editor.commit();
    }

    /**
     * Get a D2 valid server configuration, and launch the method validateCredentials
     */
    public void loginToDhis(EditText server, final EditText username, final EditText password) {
        String serverUrl = server.toString();
        if (!isEmpty(serverUrl)) {
            // configure D2
            Configuration configuration = new Configuration(server.getText().toString());
            D2.configure(configuration).
                    subscribeOn(Schedulers.io()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void v) {
                            validateCredentials(username.getText().toString(),
                                    password.getText().toString());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            handleError(throwable);
                        }
                    });
        }
    }

    /**
     * login in the dhis server and launch the onSuccess method
     */
    public void validateCredentials(String username, String password) {
        //loginView.showProgress();
        D2.me().signIn(username, password).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Action1<UserAccount>() {
                    @Override
                    public void call(
                            UserAccount userAccount) {
                        onSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleError(throwable);
                    }
                });
    }

    /**
     * Populate from database or launch the pull in the progressActivity
     */
    private void onSuccess() {

        //PullMetadata
        Log.d(TAG, "logged!");

        saveUserDetails();

        populateFromAssetsIfRequired();

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        Log.d(TAG, "Pull of programs");
        launchActivity(LoginActivity.this, ProgressActivity.class);
    }

    public void handleError(final Throwable throwable) {
        String title;
        String message;
        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        message = PreferencesState.getInstance().getContext().getText(
                                org.hisp.dhis.client.sdk.ui.bindings.R.string.error_unauthorized)
                                .toString();
                        showInvalidCredentialsError(message);
                        break;
                    }
                    case (HttpURLConnection.HTTP_BAD_GATEWAY): {
                        title = PreferencesState.getInstance().getContext().getString(
                                org.hisp.dhis.client.sdk.ui.bindings.R.string
                                        .title_error_unexpected);
                        message = exception.getMessage();
                        showErrorDialog(title, message);
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        message = PreferencesState.getInstance().getContext().getText(
                                org.hisp.dhis.client.sdk.ui.bindings.R.string.error_not_found)
                                .toString();
                        showServerError(message);
                        showErrorDialog("Error" + exception.getResponse().getStatus(),
                                exception.getResponse().getReason());
                        break;
                    }
                    default: {
                        if (exception.getCause() instanceof MalformedURLException) {
                            message = PreferencesState.getInstance().getContext().getText(
                                    org.hisp.dhis.client.sdk.ui.bindings.R.string
                                            .error_not_found).toString();
                            showServerError(message);
                            break;
                        }
                        message = exception.getMessage();
                        message = PreferencesState.getInstance().getContext().getText(
                                org.hisp.dhis.client.sdk.ui.bindings.R.string.error_message)
                                .toString();
                        showUnexpectedError(message);
                        break;
                    }
                }
            } else if (throwable.getCause() instanceof MalformedURLException) {
                message = PreferencesState.getInstance().getContext().getText(
                        org.hisp.dhis.client.sdk.ui.bindings.R.string.error_not_found).toString();
                showServerError(message);
            }
        } else {
            Log.d(TAG, "handleError", throwable);
        }
    }


    public void showServerError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        getServerUrl().setError(message);
    }

    public void showInvalidCredentialsError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        getUsername().setError(message);
        getPassword().setError(message);
    }

    public void showUnexpectedError(String message) {
        showErrorDialog(
                getString(org.hisp.dhis.client.sdk.ui.bindings.R.string.title_error_unexpected),
                message);
    }

    /**
     * Utility method to use while developing to avoid a real pull
     */
    private void populateFromAssetsIfRequired() {
        //From server -> done
        if (PreferencesState.getInstance().getPullFromServer()) {
            return;
        }

        //Populate locally
        try {
            PullController.getInstance().wipeDatabase();
            //User user = D2.me().userCredentials().first().toBlocking.first().getUsername();
            User user = new User();
            Session.setUser(user);
            PopulateDB.populateDB(getAssets());
        } catch (Exception ex) {
        }
        //Go to dashboard Activity
        launchActivity(this, DashboardActivity.class);
    }

    /**
     * Saves user credentials into preferences
     */
    private void saveUserDetails() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.dhis_url), getServerUrl().getText().toString());
        editor.putString(getString(R.string.dhis_user), getUsername().getText().toString());
        editor.putString(getString(R.string.dhis_password), getPassword().getText().toString());
        editor.commit();
    }

    /**
     * LoginActivity does NOT admin going backwads since it is always the first activity.
     * Thus onBackPressed closes the app
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showErrorDialog(String title, String message) {
        Log.d(TAG, "Login error title: " + title);
        Log.d(TAG, "Login error message: " + message);

        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to avoid leaks on configuration changes:
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}



