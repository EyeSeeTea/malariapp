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
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullDemoController;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.LoadCredentialsUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.pull.PullDemoUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.hisp.dhis.client.sdk.ui.views.FontButton;

public class LoginActivityStrategy {

    protected LoginActivity loginActivity;

    private static final String TAG = ".LoginActivityStrategy";

    public LoginActivityStrategy(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
    }

    public void onCreate() {
        if (existsLoggedUser()) {
            LoadCredentialsUseCase loadUserAndCredentialsUseCase =
                    new LoadCredentialsUseCase(loginActivity);

            loadUserAndCredentialsUseCase.execute();

            finishAndGo(DashboardActivity.class);
        } else {
            loginActivity.runOnUiThread(new Runnable() {
                public void run() {
                    addDemoButton();
                }
            });
        }
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
                    R.id.layout_login_views);

            loginActivity.getLayoutInflater().inflate(R.layout.demo_login_button, loginViewsContainer,
                    true);
    }

    private void executeDemo() {
        IMainExecutor mainExecutor = new UIThreadExecutor();
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        PullDemoController pullController = new PullDemoController(loginActivity);
        PullDemoUseCase pullUseCase = new PullDemoUseCase(pullController, mainExecutor,
                asyncExecutor);

        pullUseCase.execute(new PullDemoUseCase.Callback() {
            @Override
            public void onComplete() {
                finishAndGo(DashboardActivity.class);
            }

            @Override
            public void onPullError() {
                Log.d(this.getClass().getSimpleName(), "Pull error");
            }
        });
    }

    public void finishAndGo(Class<? extends Activity> activityClass) {
        loginActivity.startActivity(new Intent(loginActivity, activityClass));

        loginActivity.finish();
    }

}
