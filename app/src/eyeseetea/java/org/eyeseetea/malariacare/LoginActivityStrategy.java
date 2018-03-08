/*
 * Copyright (c) 2017.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.LocalPullController;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.LoadUserAndCredentialsUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;
import org.eyeseetea.malariacare.domain.usecase.pull.PullUseCase;
import org.hisp.dhis.client.sdk.ui.views.FontButton;

public class LoginActivityStrategy {

    protected LoginActivity loginActivity;

    private static final String TAG = ".LoginActivityStrategy";

    public LoginActivityStrategy(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
    }

    public void onCreate() {
        if (existsLoggedUser()) {
            LoadUserAndCredentialsUseCase loadUserAndCredentialsUseCase =
                    new LoadUserAndCredentialsUseCase(loginActivity);

            loadUserAndCredentialsUseCase.execute();

            finishAndGo(DashboardActivity.class);
        } else {
            loginActivity.runOnUiThread(new Runnable() {
                public void run() {
                    addDemoButton();
                }
            });
        }
        initAdvancedOptionsButton();
    }


    private boolean existsLoggedUser() {
        return UserDB.getLoggedUser() != null && !ProgressActivity.PULL_CANCEL;
    }

    private void addDemoButton() {

        LoginActivityStrategy.customStyle(loginActivity);

        FontButton demoButton = (FontButton) loginActivity.findViewById(R.id.demo_login_button);

        demoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Credentials demoCrededentials = Credentials.createDemoCredentials();

                loginActivity.mLoginUseCase.execute(demoCrededentials,
                        new LoginUseCase.Callback() {
                            @Override
                            public void onLoginSuccess() {
                                executeDemo();
                            }

                            @Override
                            public void onServerURLNotValid() {
                                Log.e(this.getClass().getSimpleName(), "Server url not valid");
                            }

                            @Override
                            public void onInvalidCredentials() {
                                Log.e(this.getClass().getSimpleName(), "Invalid credentials");
                            }

                            @Override
                            public void onNetworkError() {
                                Log.e(this.getClass().getSimpleName(), "Network Error");
                            }
                        });
            }
        });
    }

    private static void customStyle(LoginActivity loginActivity) {
        ViewGroup loginViewsContainer = (ViewGroup) loginActivity.findViewById(
                R.id.layout_content);

        TextView loginTextView = (TextView) loginActivity.findViewById(
                R.id.title);

        loginTextView.setVisibility(View.VISIBLE);
        loginTextView.setText(R.string.login_title);

        TextInputLayout textInputLayout = (TextInputLayout) loginActivity.findViewById(
                R.id.edittext_username_input_layout);
        modifyEditTextStyle(textInputLayout);
        textInputLayout = (TextInputLayout) loginActivity.findViewById(
                R.id.edittext_password_input_layout);
        modifyEditTextStyle(textInputLayout);
        textInputLayout = (TextInputLayout) loginActivity.findViewById(
                R.id.edittext_server_url_input_layout);
        modifyEditTextStyle(textInputLayout);
        loginTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PreferencesState.getInstance().getContext().getResources().getDimensionPixelSize(


                R.dimen.roboto_10));

        loginTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PreferencesState.getInstance().getContext().getResources().getDimensionPixelSize(
                R.dimen.roboto_10));
        //button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PreferencesState.getInstance().getContext().getResources().getDimensionPixelSize(R.dimen.roboto_16));
        loginActivity.getLayoutInflater().inflate(R.layout.demo_login_button, loginViewsContainer,
                true);
        Button button = (Button)loginViewsContainer.findViewById(R.id.button_log_in);
        LinearLayout.LayoutParams params  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(params);
        button.setBackgroundColor(ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                R.color.green_button));
        button.setTextColor(ContextCompat.getColor(PreferencesState.getInstance().getContext(), R.color.white));
        //button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PreferencesState.getInstance().getContext().getResources().getDimensionPixelSize(R.dimen.roboto_16));


    }

    private static void modifyEditTextStyle(TextInputLayout parent) {
        parent.getEditText().setHighlightColor(
                ContextCompat.getColor(PreferencesState.getInstance().getContext(), R.color.login_field));
        parent.getEditText().setHintTextColor(
                ContextCompat.getColor(PreferencesState.getInstance().getContext(), R.color.login_field));
    }

    private void executeDemo() {
        LocalPullController pullController = new LocalPullController(loginActivity);
        PullUseCase pullUseCase = new PullUseCase(pullController);

        PullFilters pullFilters = new PullFilters();

        pullUseCase.execute(pullFilters, new PullUseCase.Callback() {
            @Override
            public void onComplete() {
                finishAndGo(DashboardActivity.class);
            }

            @Override
            public void onPullError() {
                Log.d(this.getClass().getSimpleName(), "Pull error");
            }

            @Override
            public void onStep(PullStep step) {
                Log.d(this.getClass().getSimpleName(), step.toString());
            }

            @Override
            public void onCancel() {
                Log.e(this.getClass().getSimpleName(), "Pull cancel");
            }

            @Override
            public void onConversionError() {
                Log.d(this.getClass().getSimpleName(), "Pull error");
            }

            @Override
            public void onNetworkError() {
                Log.e(this.getClass().getSimpleName(), "Network Error");
            }
        });

    }

    public void finishAndGo(Class<? extends Activity> activityClass) {
        loginActivity.startActivity(new Intent(loginActivity, activityClass));

        loginActivity.finish();
    }

    private void initAdvancedOptionsButton() {
        final TextView advancedOptions = (TextView) loginActivity.findViewById(R.id.demo_advanced_options);
        advancedOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleVisibility(loginActivity.findViewById(R.id.edittext_server_url_input_layout));
                toggleText(advancedOptions, R.string.advanced_options, R.string.simple_options);
            }});
    }

    private static void toggleVisibility(View view) {
        int visibility = View.VISIBLE;
        if (view.getVisibility() == View.VISIBLE) {
            visibility = View.GONE;
        }
        view.setVisibility(visibility);
    }

    private static void toggleText(@NonNull TextView textView, @StringRes int idFirstText,
            @StringRes int idSecondText) {

        Context context = textView.getContext();
        String firstText = context.getString(idFirstText);
        String actualText = textView.getText().toString();

        if (actualText.equals(firstText)) {
            textView.setText(idSecondText);
        } else {
            textView.setText(idFirstText);
        }
    }
}
