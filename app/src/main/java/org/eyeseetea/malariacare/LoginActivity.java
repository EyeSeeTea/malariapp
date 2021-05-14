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

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;

import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.LanguageContextWrapper;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.remote.api.PullDhisApiDataSource;
import org.eyeseetea.malariacare.domain.common.Either;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.Server;
import org.eyeseetea.malariacare.domain.usecase.GetServerUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.factories.AuthenticationFactory;
import org.eyeseetea.malariacare.factories.ServerFactory;
import org.eyeseetea.malariacare.layout.adapters.general.ServerArrayAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.presentation.bugs.BugReportKt;
import org.eyeseetea.malariacare.presentation.presenters.LoginPresenter;
import org.eyeseetea.malariacare.strategies.LoginActivityStrategy;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Permissions;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.io.InputStream;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

public class LoginActivity extends Activity implements LoginPresenter.View {
    private static final String TAG = ".LoginActivity";

    public LoginActivityStrategy mLoginActivityStrategy = new LoginActivityStrategy(this);

    private CircularProgressBar progressBar;
    private ViewGroup loginViewsContainer;
    private Spinner serverSpinner;
    private LinearLayout serverContainer;
    private static LoginActivity mLoginActivity;

    private EditText mServerUrl;
    private EditText mUsername;
    private EditText mPassword;
    private Button mLoginButton;

    // Action which should be executed after animation is finished
    private OnPostAnimationRunnable onPostAnimationAction;

    private OnPostAnimationListener onPostAnimationListener;


    // LayoutTransition (for JellyBean+ devices only)
    private LayoutTransition layoutTransition;

    // Animations for pre-JellyBean devices
    private Animation layoutTransitionSlideIn;
    private Animation layoutTransitionSlideOut;

    private LoginPresenter loginPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLoginActivity = this;
        requestPermissions();
        PreferencesState.getInstance().initalizateActivityDependencies();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLoginActivityStrategy.onCreate();

        UserDB loggedUser = UserDB.getLoggedUser();

        if (loggedUser != null && !ProgressActivity.PULL_CANCEL
                && sharedPreferences.getBoolean(
                getApplicationContext().getResources().getString(R.string.pull_metadata), false)) {

            GetServerUseCase getServerUseCase = ServerFactory.INSTANCE.provideGetServerUseCase(this);

            getServerUseCase.execute(serverResult -> {
                Server server = ((Either.Right<Server>) serverResult).getValue();

                BugReportKt.addServerAndUser(server.getUrl(),loggedUser.getUsername());
                launchActivity(LoginActivity.this, DashboardActivity.class);
            });
        } else {
            ProgressActivity.PULL_CANCEL = false;
            BugReportKt.removeServerAndUser();

            initViews();
            initPresenter();
            initServerAdapter();
        }
    }

    private void initViews() {
        serverSpinner = findViewById(R.id.server_spinner);
        serverContainer = findViewById(R.id.edittext_server_url_container);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                enableOrDisableLoginButton();
            }
        };


        mServerUrl = findViewById(R.id.edittext_server_url);
        mServerUrl.addTextChangedListener(watcher);
        mUsername = findViewById(R.id.edittext_username);
        mUsername.addTextChangedListener(watcher);
        mPassword = findViewById(R.id.edittext_password);
        mPassword.addTextChangedListener(watcher);
        mLoginButton = findViewById(R.id.button_log_in);

        mServerUrl.setText(R.string.login_info_dhis_default_server_url);

        float progressBarStrokeWidth = getResources()
                .getDimensionPixelSize(R.dimen.progressbar_stroke_width);
        progressBar = findViewById(R.id.progress_bar_circular);
        progressBar.setIndeterminateDrawable(new CircularProgressDrawable.Builder(this)
                .color(ContextCompat.getColor(this,
                        R.color.color_primary_default))
                .style(CircularProgressDrawable.STYLE_ROUNDED)
                .strokeWidth(progressBarStrokeWidth)
                .rotationSpeed(1f)
                .sweepSpeed(1f)
                .build());

        replaceDhisLogoToHNQISLogo();

        loginViewsContainer = (CardView) findViewById(R.id.layout_login_views);

        mLoginButton.setOnClickListener(
                v -> onLoginButtonClicked(mServerUrl.getText(), mUsername.getText(),
                        mPassword.getText()));
        mLoginButton.setEnabled(false);

        onPostAnimationListener = new OnPostAnimationListener();

        if (isGreaterThanOrJellyBean()) {
            layoutTransition = new LayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            layoutTransition.addTransitionListener(onPostAnimationListener);

            RelativeLayout loginLayoutContent = findViewById(R.id.layout_content);
            loginLayoutContent.setLayoutTransition(layoutTransition);
        } else {
            layoutTransitionSlideIn = AnimationUtils.loadAnimation(this,
                    R.anim.in_up);
            layoutTransitionSlideOut = AnimationUtils.loadAnimation(this,
                    R.anim.out_down);

            layoutTransitionSlideIn.setAnimationListener(onPostAnimationListener);
            layoutTransitionSlideOut.setAnimationListener(onPostAnimationListener);
        }

        hideProgress();
    }

    private void enableOrDisableLoginButton() {
        String url = mServerUrl.getText().toString();
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();

        mLoginButton.setEnabled((url != null && !url.equals("") &&
                username != null && !username.equals("") &&
                password != null && !password.equals("")));
    }

    private boolean isGreaterThanOrJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    @Override
    protected void onDestroy() {
        if (loginPresenter != null) {
            loginPresenter.detachView();
        }

        super.onDestroy();
    }

    private void initPresenter() {
        loginPresenter = AuthenticationFactory.INSTANCE.provideLoginPresenter(this);

        loginPresenter.attachView(this, getResources().getString(R.string.other));
    }

    private void initServerAdapter() {
        serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Server server = (Server) parent.getItemAtPosition(position);
                loginPresenter.selectServer(server);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void replaceDhisLogoToHNQISLogo() {
        FrameLayout progressBarContainer = (FrameLayout) findViewById(R.id.layout_dhis_logo);
        ((CustomTextView) progressBarContainer.getChildAt(2)).setText("");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        progressBarContainer.addView(inflater.inflate(R.layout.progress_logo_item, null));
    }

    private void launchActivity(Activity activity, Class<?> activityClass) {
        Intent intent = new Intent(activity, activityClass);
        ActivityCompat.startActivity(LoginActivity.this, intent, null);
    }

    private void onLoginButtonClicked(Editable server, Editable username, Editable password) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(getString(R.string.eula_accepted), false)) {
            askEula(R.string.settings_menu_eula, R.raw.eula, LoginActivity.this);
        } else {
            login(mServerUrl.getText().toString(), mUsername.getText().toString(),
                    mPassword.getText().toString());
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
                        login(mServerUrl.getText().toString(),
                                mUsername.getText().toString(),
                                mPassword.getText().toString());
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

        LoginUseCase mLoginUseCase = AuthenticationFactory.INSTANCE.provideLoginUseCase(this);

        mLoginUseCase.execute(credentials,
                new LoginUseCase.Callback() {
                    @Override
                    public void onLoginSuccess() {
                        PreferencesState.getInstance().setUserAccept(false);
                        hideProgress();
                        AsyncPullAnnouncement
                                asyncPullAnnouncement = new AsyncPullAnnouncement();
                        asyncPullAnnouncement.execute(mLoginActivity);

                        BugReportKt.addServerAndUser(serverUrl, username);
                    }

                    @Override
                    public void onServerURLNotValid() {
                        showError(PreferencesState.getInstance().getContext().getText(
                                R.string.error_not_found).toString());
                    }

                    @Override
                    public void onInvalidCredentials() {
                        showError(PreferencesState.getInstance().getContext().getText(
                                R.string.error_unauthorized).toString());
                    }

                    @Override
                    public void onNetworkError() {
                        showError(PreferencesState.getInstance().getContext().getString(
                                R.string.network_error));
                    }

                    @Override
                    public void onRequiredAuthorityError(String authority) {
                        showError(PreferencesState.getInstance().getContext().getString(
                                R.string.required_authority_error));
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
        LogoutUseCase logoutUseCase = AuthenticationFactory.INSTANCE.provideLogoutUseCase(this);

        logoutUseCase.execute(new LogoutUseCase.Callback() {
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
        Log.d(TAG, "Showing progress");
        if (layoutTransitionSlideOut != null) {
            loginViewsContainer.startAnimation(layoutTransitionSlideOut);
        }

        loginViewsContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        Log.d(TAG, "Hiding progress");
        if (layoutTransitionSlideIn != null) {
            loginViewsContainer.startAnimation(layoutTransitionSlideIn);
        }

        loginViewsContainer.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
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

    @Override
    public void showLoading() {
        showProgress();
    }

    @Override
    public void hideLoading() {
        hideProgress();
    }

    @Override
    public void showServers(List<Server> servers) {
        ArrayAdapter serversListAdapter =
                new ServerArrayAdapter(LoginActivity.this, servers);
        serverSpinner.setAdapter(serversListAdapter);

        mServerUrl.setText(servers.get(0).getUrl());
    }

    @Override
    public void showManualServerUrlView() {
        mServerUrl.setText("");
        serverContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideManualServerUrlView(String serverUrl) {
        mServerUrl.setText(serverUrl);
        serverContainer.setVisibility(View.GONE);
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

    private static class OnPostAnimationRunnable implements Runnable {
        private final OnAnimationFinishListener listener;
        private final LoginActivity loginActivity;
        private final boolean showProgress;

        public OnPostAnimationRunnable(OnAnimationFinishListener listener,
                LoginActivity loginActivity, boolean showProgress) {
            this.listener = listener;
            this.loginActivity = loginActivity;
            this.showProgress = showProgress;
        }

        @Override
        public void run() {
            if (loginActivity != null) {
                if (showProgress) {
                    loginActivity.showProgress();
                } else {
                    loginActivity.hideProgress();
                }
            }

            if (listener != null) {
                listener.onFinish();
            }
        }

        public boolean isProgressBarWillBeShown() {
            return showProgress;
        }
    }

    protected interface OnAnimationFinishListener {
        void onFinish();
    }

    private class OnPostAnimationListener implements LayoutTransition.TransitionListener,
            Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            // stub implementation
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // stub implementation
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            onPostAnimation();
        }

        @Override
        public void startTransition(
                LayoutTransition transition, ViewGroup container, View view, int type) {
            // stub implementation
        }

        @Override
        public void endTransition(
                LayoutTransition transition, ViewGroup container, View view, int type) {
            if (LayoutTransition.CHANGE_APPEARING == type ||
                    LayoutTransition.CHANGE_DISAPPEARING == type) {
                onPostAnimation();
            }
        }

        private void onPostAnimation() {
            if (onPostAnimationAction != null) {
                onPostAnimationAction.run();
                onPostAnimationAction = null;
            }
        }
    }

}