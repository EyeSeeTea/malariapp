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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.eyeseetea.malariacare.data.database.datasources.UserAccountLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.LanguageContextWrapper;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.remote.api.UserAccountAPIDataSource;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.enums.NetworkStrategy;
import org.eyeseetea.malariacare.domain.usecase.GetUserAccountUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.factories.AuthenticationFactory;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Permissions;

import java.io.InputStream;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;


public class LoginActivity extends Activity {
    private static final String TAG = ".LoginActivity";
    private static final String IS_LOADING = "state:isLoading";

    public LoginUseCase mLoginUseCase = new AuthenticationFactory().getLoginUseCase(this);
    LogoutUseCase mLogoutUseCase = new AuthenticationFactory().getLogoutUseCase(this);
    public LoginActivityStrategy mLoginActivityStrategy = new LoginActivityStrategy(this);

    private CircularProgressBar progressBar;
    private ViewGroup loginViewsContainer;
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
        if (UserDB.getLoggedUser() != null && !ProgressActivity.PULL_CANCEL
                && sharedPreferences.getBoolean(
                getApplicationContext().getResources().getString(R.string.pull_metadata), false)) {
            launchActivity(LoginActivity.this, DashboardActivity.class);
        }
        ProgressActivity.PULL_CANCEL = false;
        initViews();

    }

    private void initViews() {
        mServerUrl = (EditText) findViewById(R.id.edittext_server_url);
        mUsername = (EditText) findViewById(R.id.edittext_username);
        mPassword = (EditText) findViewById(R.id.edittext_password);
        mLoginButton = (Button) findViewById(R.id.button_log_in);
        mServerUrl.setText(R.string.login_info_dhis_default_server_url);

        float progressBarStrokeWidth = getResources()
                .getDimensionPixelSize(R.dimen.progressbar_stroke_width);
        progressBar = (CircularProgressBar) findViewById(R.id.progress_bar_circular);
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

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginButtonClicked(mServerUrl.getText(), mUsername.getText(),
                        mPassword.getText());
            }
        });

        onPostAnimationListener = new OnPostAnimationListener();

        if (isGreaterThanOrJellyBean()) {
            layoutTransition = new LayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            layoutTransition.addTransitionListener(onPostAnimationListener);

            RelativeLayout loginLayoutContent = (RelativeLayout) findViewById(
                    R.id.layout_content);
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

    private static boolean isGreaterThanOrJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    @Override
    protected final void onSaveInstanceState(Bundle outState) {
        if (onPostAnimationAction != null) {
            outState.putBoolean(IS_LOADING,
                    onPostAnimationAction.isProgressBarWillBeShown());
        } else {
            outState.putBoolean(IS_LOADING, progressBar.isShown());
        }

        super.onSaveInstanceState(outState);
    }

    private void replaceDhisLogoToHNQISLogo() {
        FrameLayout progressBarContainer = (FrameLayout) findViewById(R.id.layout_dhis_logo);
        ((TextView) progressBarContainer.getChildAt(
                2)).setText("");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        progressBarContainer.addView(inflater.inflate(R.layout.progress_logo_item, null));
    }

    private void launchActivity(Activity activity, Class<?> activityClass) {
        Intent intent = new Intent(activity, activityClass);
        ActivityCompat.startActivity(LoginActivity.this, intent, null);
    }

    protected void onLoginButtonClicked(Editable server, Editable username, Editable password) {
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
        mLoginActivityStrategy.login();

        final Credentials credentials = new Credentials(serverUrl, username, password);
        mLoginUseCase.execute(credentials, new LoginUseCase.Callback() {
            @Override
            public void onLoginSuccess() {
                hideProgress();
                GetUserAccountUseCase getUserAccountUseCase = new GetUserAccountUseCase(
                        new AsyncExecutor(),
                        new UIThreadExecutor(),
                        new UserAccountRepository(
                                new UserAccountAPIDataSource(Session.getCredentials()),
                                new UserAccountLocalDataSource())
                );

                getUserAccountUseCase.execute(NetworkStrategy.NETWORK_FIRST,
                        new GetUserAccountUseCase.Callback() {
                            @Override
                            public void onSuccess(UserAccount userAccount) {
                                onGetUserSuccess(userAccount.isClosed());
                            }

                            @Override
                            public void onError() {
                                System.out.println("Error pulling closed user date.");
                            }
                        });
            }

            @Override
            public void onServerURLNotValid() {
                mLoginActivityStrategy.onLoginError();
                showError(PreferencesState.getInstance().getContext().getText(
                        R.string
                                .error_not_found).toString());
            }

            @Override
            public void onInvalidCredentials() {
                mLoginActivityStrategy.onLoginError();
                showError(PreferencesState.getInstance().getContext().getText(
                        R.string.error_unauthorized)
                        .toString());
            }

            @Override
            public void onNetworkError() {
                mLoginActivityStrategy.onLoginError();
                showError(PreferencesState.getInstance().getContext().getString(
                        R.string
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

    public void onGetUserSuccess(boolean isClosed) {
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

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLanguage = PreferencesState.getInstance().getCurrentLocale();
        Context context = LanguageContextWrapper.wrap(newBase, currentLanguage);
        super.attachBaseContext(context);
    }

    /* since this runnable is intended to be executed on UI (not main) thread, we should
    be careful and not keep any implicit references to activities */
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



