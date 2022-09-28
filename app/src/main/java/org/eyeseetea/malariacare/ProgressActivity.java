/*
 * Copyright (c) 2015.
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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.LocalPullController;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.boundary.IPullController;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;
import org.eyeseetea.malariacare.domain.usecase.pull.PullUseCase;
import org.eyeseetea.malariacare.factories.DataFactory;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;

import java.util.Calendar;

public class ProgressActivity extends Activity {

    public static final int NUMBER_OF_MONTHS = 12;
    /**
     * Intent param that tells what to do (push, pull or push before pull)
     */
    public static final String TYPE_OF_ACTION = "TYPE_OF_ACTION";
    /**
     * Intent param that tells what do before push
     */
    public static final String AFTER_ACTION = "AFTER_ACTION";
    /**
     * To dont show the survey pushed feedback
     */
    public static final int DONT_SHOW_FEEDBACK = 1;
    /**
     * To show the survey pushed feedback
     */
    public static final int SHOW_FEEDBACK = 2;
    /**
     * To push every unsent data to server before pulling metadata
     */
    public static final int ACTION_PUSH_BEFORE_PULL = 2;
    private static final String TAG = ".ProgressActivity";
    /**
     * Num of expected steps while pulling
     */
    //// FIXME: 28/11/2016 revise the max num of pull steps
    private static final int MAX_PULL_STEPS = 10;

    /**
     * Used for control autopull from login
     */
    public static Boolean PULL_CANCEL = false;

    /**
     * Used for control error in pull
     */
    public static Boolean PULL_ERROR = false;
    /**
     * Reference to progress indicator
     */
    public static ProgressBar progressBar;
    /**
     * Reference to progress message
     */
    public static TextView textView;
    static boolean isOnPause = true;
    //Check intent params
    static Intent intent;
    public PullUseCase mPullUseCase;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        PreferencesState.getInstance().initalizateActivityDependencies();
        initializeDependencies();
        setContentView(R.layout.activity_progress);
        PULL_CANCEL = false;
        isOnPause = false;
        prepareUI();
        final Button button = (Button) findViewById(R.id.cancelPullButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancelPull();
            }
        });
        intent = getIntent();
        handler = new Handler();
    }

    private void initializeDependencies() {
        IPullController pullController;
        if (Session.getCredentials() == null || Session.getCredentials().isDemoCredentials()) {
            pullController = new LocalPullController(this);
        } else {
            pullController = new PullController(new ServerMetadataRepository(this));
        }
        mPullUseCase = new PullUseCase(pullController);
    }

    /**
     * Prints the step in the progress bar
     */
    public void step(final String msg) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int currentProgress = progressBar.getProgress();
                progressBar.setProgress(currentProgress + 1);
                textView = (TextView) findViewById(R.id.pull_text);
                textView.setText(msg);

            }
        });
    }

    private static void annotateFirstPull(boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                PreferencesState.getInstance().getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(
                PreferencesState.getInstance().getContext().getString(R.string.pull_metadata),
                value);
        editor.commit();
    }

    private static String getDialogMessage(String msg) {
        if (msg != null) {
            return msg;
        }
        return "";
    }

    private static String getDialogTitle() {
        int stringId = R.string.dialog_title_pull_response;
        return PreferencesState.getInstance().getContext().getString(stringId);
    }

    private void cancelPull() {
        step(getBaseContext().getResources().getString(R.string.cancellingPull));
        mPullUseCase.cancel();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (!isOnPause) {
            launchPull();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (PULL_CANCEL == true) {
            finishAndGo(LoginActivity.class);
        }
        isOnPause = true;
    }

    private void prepareUI() {
        progressBar = (ProgressBar) findViewById(R.id.pull_progress);
        progressBar.setMax(MAX_PULL_STEPS);
        textView = (TextView) findViewById(R.id.pull_text);
    }

    /**
     * Shows a dialog with the given message y move to login after showing error
     */
    private void showException(final String msg) {
        Log.d(TAG, msg + " ");

        PULL_ERROR = true;
        PULL_CANCEL = true;
        final String dialogTitle = getDialogTitle();
        final String dialogMessage = getDialogMessage(msg);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() { // This thread runs in the UI
                    @Override
                    public void run() {
                        new AlertDialog.Builder(ProgressActivity.this)
                                .setCancelable(false)
                                .setTitle(dialogTitle)
                                .setMessage(dialogMessage)
                                .setNeutralButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                executeLogout();
                                            }
                                        }).create().show();
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

    private void executeLogout() {
        Log.d(TAG, "Logging out...");
        UserAccountRepository userAccountRepository = new UserAccountRepository(this);
        LogoutUseCase logoutUseCase = new LogoutUseCase(userAccountRepository);

        logoutUseCase.execute(new LogoutUseCase.Callback() {
            @Override
            public void onLogoutSuccess() {
                finishAndGo(LoginActivity.class);
            }

            @Override
            public void onLogoutError(String message) {
                Log.e(TAG, message);
            }
        });
    }

    /**
     * Shows a dialog to tell that pull is done and then moves into the dashboard.
     */
    private void showAndMoveOn() {
        if (PULL_ERROR) {
            PULL_ERROR = false;
            finishAndGo(LoginActivity.class);
            return;
        }

        //If is not active, we need restart the process
        if (!mPullUseCase.isPullActive()) {
            finishAndGo(LoginActivity.class);
            return;
        }

        //Show final step -> done
        step(getString(R.string.progress_pull_done));

        final String title = getDialogTitle();

        final int msg = getDoneMessage();

        Log.i(TAG, getString(msg));


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() { // This thread runs in the UI
                    @Override
                    public void run() {
                        AlertDialog alertDialog = new AlertDialog.Builder(ProgressActivity.this)
                                .setCancelable(false)
                                .setTitle(title)
                                .setMessage(msg)
                                .setNeutralButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                                finishAndGo(DashboardActivity.class);
                                                return;
                                            }
                                        }).create();
                        alertDialog.show();
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

    private int getDoneMessage() {
        if (Session.getCredentials().isDemoCredentials()) {
            return R.string.dialog_demo_pull_success;
        }
        return R.string.dialog_pull_success;
    }

    private void launchPull() {
        annotateFirstPull(false);
        progressBar.setProgress(0);
        progressBar.setMax(MAX_PULL_STEPS);
        Calendar month = Calendar.getInstance();
        month.add(Calendar.MONTH, -NUMBER_OF_MONTHS);
        boolean isDemo = Session.getCredentials().equals(Credentials.createDemoCredentials());
        PullFilters pullFilters = new PullFilters(month.getTime(), null, isDemo,
                AppSettingsBuilder.isFullHierarchy(), AppSettingsBuilder.isDownloadOnlyLastEvents(),
                PreferencesState.getInstance().getMaxEvents());

        mPullUseCase.execute(pullFilters, new PullUseCase.Callback() {
            @Override
            public void onComplete() {
                postFinish();
                DataFactory.INSTANCE.reset();
            }

            @Override
            public void onPullError() {
                showException(getBaseContext().getString(R.string
                        .dialog_pull_error));
            }

            @Override
            public void onCancel() {
                showException(getBaseContext().getString(R.string
                        .pull_cancelled));
            }

            @Override
            public void onConversionError() {
                showException(getBaseContext().getString(R.string
                        .error_in_pull_conversion));
            }

            @Override
            public void onStep(PullStep pullStep) {
                switch (pullStep) {
                    case PROGRAMS:
                        step(PreferencesState.getInstance().getContext().getString(
                                R.string.progress_pull_downloading));
                        break;
                    case EVENTS:
                        step(PreferencesState.getInstance().getContext().getString(
                                R.string.progress_push_preparing_events));
                        break;
                    case PREPARING_PROGRAMS:
                        step(PreferencesState.getInstance().getContext().getString(
                                R.string.progress_pull_preparing_program));
                        break;
                    case PREPARING_ANSWERS:
                        step(PreferencesState.getInstance().getContext().getString(
                                R.string.progress_pull_preparing_answers));
                        break;
                    case PREPARING_ORGANISATION_UNITS:
                        step(PreferencesState.getInstance().getContext().getString(
                                R.string.progress_pull_preparing_orgs));
                        break;
                    case PREPARING_QUESTIONS:
                        step(PreferencesState.getInstance().getContext().getString(
                                R.string.progress_pull_questions));
                        break;
                    case PREPARING_RELATIONSHIPS:
                        step(PreferencesState.getInstance().getContext().getString(
                                R.string.progress_pull_relationships));
                        break;
                    case PREPARING_SURVEYS:
                        step(PreferencesState.getInstance().getContext().getString(
                                R.string.progress_pull_surveys));
                        break;
                    case VALIDATE_COMPOSITE_SCORES:
                        step(PreferencesState.getInstance().getContext().getString(
                                R.string.progress_pull_validating_composite_scores));
                        break;
                }

            }

            @Override
            public void onNetworkError() {
                showException(PreferencesState.getInstance().getContext().getString(
                        R.string.title_error_unexpected));
            }
        });
    }

    /**
     * Finish current activity and launches an activity with the given class
     *
     * @param targetActivityClass Given target activity class
     */
    public void finishAndGo(Class targetActivityClass) {
        Intent targetActivityIntent = new Intent(this, targetActivityClass);
        this.finish();
        this.startActivity(targetActivityIntent);
    }

    public void postFinish() {
        Log.d(TAG, "post finish");
        annotateFirstPull(true);
        showAndMoveOn();
    }
}
