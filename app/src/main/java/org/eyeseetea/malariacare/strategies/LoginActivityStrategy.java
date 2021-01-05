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

package org.eyeseetea.malariacare.strategies;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.LocalPullController;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.LoadUserAndCredentialsUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;
import org.eyeseetea.malariacare.domain.usecase.pull.PullUseCase;
import org.eyeseetea.malariacare.factories.AuthenticationFactory;
import org.eyeseetea.malariacare.views.CustomButton;

public class LoginActivityStrategy {

    protected LoginActivity loginActivity;

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
            addDemoButton();
        }
    }


    private boolean existsLoggedUser() {
        return UserDB.getLoggedUser() != null && !ProgressActivity.PULL_CANCEL;
    }

    private void addDemoButton() {
        CustomButton demoButton = loginActivity.findViewById(R.id.demo_login_button);

        demoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Credentials demoCredentials = Credentials.createDemoCredentials();

                LoginUseCase mLoginUseCase = AuthenticationFactory.INSTANCE.provideLoginUseCase(
                        loginActivity);

                mLoginUseCase.execute(demoCredentials,
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

                            @Override
                            public void onRequiredAuthorityError(String authority) {
                                Log.e(this.getClass().getSimpleName(), "Required Authority Error");
                            }

                            @Override
                            public void onUnsupportedServerVersion() {
                                Log.e(this.getClass().getSimpleName(),
                                        "Unsupported Server Version");
                            }
                        });
            }
        });
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

}
