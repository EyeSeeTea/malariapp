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
import android.os.AsyncTask;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eyeseetea.malariacare.data.database.datasources.ServerInfoLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.LanguageContextWrapper;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.remote.api.PullDhisApiDataSource;
import org.eyeseetea.malariacare.data.remote.api.ServerInfoRemoteDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.Server;
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.factories.ServerFactory;
import org.eyeseetea.malariacare.layout.adapters.general.ServerArrayAdapter;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.strategies.LoginActivityStrategy;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Permissions;
import org.hisp.dhis.client.sdk.ui.activities.AbsLoginActivity;

import java.io.InputStream;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class LoginActivity extends AbsLoginActivity {
    private static final String TAG = ".LoginActivity";

    public IUserAccountRepository mUserAccountRepository = new UserAccountRepository(this);
    IAsyncExecutor asyncExecutor = new AsyncExecutor();
    IMainExecutor mainExecutor = new UIThreadExecutor();
    LogoutUseCase mLogoutUseCase = new LogoutUseCase(mUserAccountRepository);
    public LoginActivityStrategy mLoginActivityStrategy = new LoginActivityStrategy(this);

    private CircularProgressBar progressBar;
    private ViewGroup loginViewsContainer;
    private Spinner serverSpinner;
    private LinearLayout serverContainer;
    private EditText serverEditText;
    private static LoginActivity mLoginActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mLoginActivity = this;
        requestPermissions();
        PreferencesState.getInstance().initalizateActivityDependencies();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLoginActivityStrategy.onCreate();
        if (UserDB.getLoggedUser() != null && !ProgressActivity.PULL_CANCEL
                && sharedPreferences.getBoolean(
                getApplicationContext().getResources().getString(R.string.pull_metadata), false)) {
            launchActivity(LoginActivity.this, DashboardActivity.class);
        }
        ProgressActivity.PULL_CANCEL = false;
        getServerUrl().setText(R.string.login_info_dhis_default_server_url);

        progressBar = (CircularProgressBar) findViewById(R.id.progress_bar_circular);

        replaceDhisLogoToHNQISLogo();

        loginViewsContainer = (CardView) findViewById(R.id.layout_login_views);

        serverSpinner = (Spinner) findViewById(R.id.server_spinner);
        serverContainer = (LinearLayout) findViewById(R.id.edittext_server_url_container);
        serverEditText = (EditText) findViewById(R.id.edittext_server_url);

        initServerAdapter();
    }

    private void initServerAdapter() {

        ServerFactory serverFactory = new ServerFactory();

        GetServersUseCase getServersUseCase = serverFactory.getServersUseCase(this);
        getServersUseCase.execute(servers -> {
            ArrayAdapter serversListAdapter =
                    new ServerArrayAdapter(LoginActivity.this, servers);
            serverSpinner.setAdapter(serversListAdapter);
        });


        serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Server server =(Server) parent.getItemAtPosition(position);
                if (server.getUrl().equals(parent.getContext().getResources().getString(R.string.other))) {
                    serverEditText.setText("");
                    serverContainer.setVisibility(View.VISIBLE);
                } else {
                    if (serverContainer.getVisibility() == View.VISIBLE) {
                        serverContainer.setVisibility(View.GONE);
                    }
                    serverEditText.setText(server.getUrl());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);
            }
        });
    }

    private void replaceDhisLogoToHNQISLogo() {
        FrameLayout progressBarContainer = (FrameLayout) findViewById(R.id.layout_dhis_logo);
        ((org.hisp.dhis.client.sdk.ui.views.FontTextView) progressBarContainer.getChildAt(
                2)).setText("");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        progressBarContainer.addView(inflater.inflate(R.layout.progress_logo_item, null));
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

    public void login(String serverUrl, String username, String password) {
        showProgress();

        final Credentials credentials = new Credentials(serverUrl, username, password);

        ServerInfoLocalDataSource mServerLocalDataSource = new ServerInfoLocalDataSource(
                getApplicationContext());
        ServerInfoRemoteDataSource mServerRemoteDataSource = new ServerInfoRemoteDataSource(
                credentials);
        ServerInfoRepository serverInfoRepository = new ServerInfoRepository(mServerLocalDataSource,
                mServerRemoteDataSource);

        ServerFactory serverFactory = new ServerFactory();
        IServerRepository serverRepository = serverFactory.getServerRepository(this);

        LoginUseCase mLoginUseCase = new LoginUseCase(mUserAccountRepository,serverRepository,
                serverInfoRepository, mainExecutor, asyncExecutor);
        mLoginUseCase.execute(credentials,
                new LoginUseCase.Callback() {
                    @Override
                    public void onLoginSuccess() {
                        PreferencesState.getInstance().setUserAccept(false);
                        hideProgress();
                        AsyncPullAnnouncement
                                asyncPullAnnouncement = new AsyncPullAnnouncement();
                        asyncPullAnnouncement.execute(mLoginActivity);
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

                    @Override
                    public void onUnsupportedServerVersion() {
                        showError(PreferencesState.getInstance().getContext().getString(
                                R.string.login_error_unsupported_server_version));

                    }
                });
    }

    public void showError(String message) {
        hideProgress();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void onSuccess(boolean isClosed) {
        if (isClosed) {
            closeUser(R.string.admin_announcement,
                    PreferencesState.getInstance().getContext().getString(R.string.user_close),
                    mLoginActivity);
        } else {
            launchActivity(LoginActivity.this, ProgressActivity.class);
        }
    }

    private void closeUser(int titleId, String message, LoginActivity context) {

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferencesState.getInstance().setUserAccept(false);
                LoginActivity.mLoginActivity.executeLogout();
            }
        };
        AUtils.closeUser(titleId, message, context, listener);
    }

    //Todo: This code is repeated in DashboardActivity
    public void executeLogout() {
        mLogoutUseCase.execute(new LogoutUseCase.Callback() {
            @Override
            public void onLogoutSuccess() {
                Log.e("." + this.getClass().getSimpleName(), "Logout success");
            }

            @Override
            public void onLogoutError(String message) {
                Log.e("." + this.getClass().getSimpleName(), message);
            }
        });
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
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
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

    public void requestPermissions() {
        if (EyeSeeTeaApplication.permissions == null) {
            EyeSeeTeaApplication.permissions = Permissions.getInstance(this);
        }
        if (!EyeSeeTeaApplication.permissions.areAllPermissionsGranted()) {
            EyeSeeTeaApplication.permissions.requestNextPermission();
        }
    }

    public class AsyncPullAnnouncement extends AsyncTask<LoginActivity, Void, Void> {
        //userCloseChecker is never saved, Only for check if the date is closed.
        LoginActivity loginActivity;
        boolean isUserClosed = false;

        @Override
        protected Void doInBackground(LoginActivity... params) {
            loginActivity = params[0];
            isUserClosed = PullDhisApiDataSource.isUserClosed(Session.getUser().getUid());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            onSuccess(isUserClosed);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLanguage = PreferencesState.getInstance().getCurrentLocale();
        Context context = LanguageContextWrapper.wrap(newBase, currentLanguage);
        super.attachBaseContext(context);
    }
}



