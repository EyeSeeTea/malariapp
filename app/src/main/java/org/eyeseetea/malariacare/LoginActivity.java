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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import org.eyeseetea.malariacare.data.database.model.User;
import org.eyeseetea.malariacare.data.database.utils.PopulateDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.boundary.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.LoadUserAndCredentialsUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.strategies.LoginActivityStrategy;
import org.eyeseetea.malariacare.utils.AUtils;
import org.hisp.dhis.client.sdk.ui.activities.AbsLoginActivity;

import java.io.InputStream;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class LoginActivity extends AbsLoginActivity {
    private static final String TAG = "LoginActivity";

    public IUserAccountRepository mUserAccountRepository = new UserAccountRepository(this);
    public LoginUseCase mLoginUseCase = new LoginUseCase(mUserAccountRepository);
    public LoginActivityStrategy mLoginActivityStrategy = new LoginActivityStrategy(this);

    private CircularProgressBar progressBar;
    private ViewGroup loginViewsContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLoginActivityStrategy.onCreate();
        if (User.getLoggedUser() != null && !ProgressActivity.PULL_CANCEL
                && sharedPreferences.getBoolean(
                getApplicationContext().getResources().getString(R.string.pull_metadata), false)) {
            launchActivity(LoginActivity.this, DashboardActivity.class);
        }
        ProgressActivity.PULL_CANCEL = false;
        getServerUrl().setText(R.string.login_info_dhis_default_server_url);

        progressBar = (CircularProgressBar) findViewById(R.id.progress_bar_circular);
        loginViewsContainer = (CardView) findViewById(R.id.layout_login_views);

    }

    private void launchActivity(Activity activity, Class<?> activityClass) {
        Intent intent = new Intent(activity, activityClass);
        ActivityCompat.startActivity(LoginActivity.this, intent, null);
    }

    @Override
    protected void onLoginButtonClicked(Editable server, Editable username, Editable password) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(getString(R.string.eula_accepted), false)) {
            askEula(R.string.settings_menu_eula, R.raw.eula, LoginActivity.this);
        } else {
            login(getServerUrl().getText().toString(), getUsername().getText().toString(),
                    getPassword().getText().toString());
        }
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
                        login(getServerUrl().getText().toString(),
                                getUsername().getText().toString(),
                                getPassword().getText().toString());
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

    public void login(String serverUrl, String username, String password) {
        showProgress();

        final Credentials credentials = new Credentials(serverUrl, username, password);
        mLoginUseCase.execute(credentials, new LoginUseCase.Callback() {
            @Override
            public void onLoginSuccess() {
                onSuccess();
            }

            @Override
            public void onServerURLNotValid() {
                showError(PreferencesState.getInstance().getContext().getText(
                        org.hisp.dhis.client.sdk.ui.bindings.R.string
                                .error_not_found).toString());
            }

            @Override
            public void onInvalidCredentials() {
                showError(PreferencesState.getInstance().getContext().getText(
                        org.hisp.dhis.client.sdk.ui.bindings.R.string.error_unauthorized)
                        .toString());
            }

            @Override
            public void onNetworkError() {
                showError(PreferencesState.getInstance().getContext().getString(
                        org.hisp.dhis.client.sdk.ui.bindings.R.string
                                .title_error_unexpected));
            }
        });
    }

    public void showError(String message) {
        hideProgress();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void onSuccess() {
        hideProgress();

        populateFromAssetsIfRequired();

        launchActivity(LoginActivity.this, ProgressActivity.class);
    }

    public void showProgress() {
        hideSoftKeyboard();
        loginViewsContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        loginViewsContainer.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
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
            PopulateDB.wipeDatabase();
            PopulateDB.populateDB(getAssets());
        } catch (Exception ex) {
        }
        //Go to dashboard Activity
        launchActivity(this, DashboardActivity.class);
    }
}



