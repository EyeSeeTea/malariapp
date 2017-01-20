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
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushController;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.SyncProgressStatus;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.SdkController;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ProgressActivity extends Activity {

    private static final String TAG = ".ProgressActivity";

    /**
     * Intent param that tells what to do (push, pull or push before pull)
     */
    public static final String TYPE_OF_ACTION = "TYPE_OF_ACTION";

    /**
     * Intent param that tells what do before push
     */
    public static final String AFTER_ACTION = "AFTER_ACTION";
    /**
     * To pull data from server
     */
    public static final int ACTION_PULL = 0;

    /**
     * To push a single survey to server
     */
    public static final int ACTION_PUSH = 1;

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

    /**
     * Num of expected steps while pulling
     */
    //// FIXME: 28/11/2016 revise the max num of pull steps
    private static final int MAX_PULL_STEPS = 7;

    /**
     * Num of expected steps while pushing
     */
    private static final int MAX_PUSH_STEPS = 4;
    /**
     * Used for control new steps
     */
    public static Boolean PULL_IS_ACTIVE = false;

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
    /**
     * Reference required for testing purposes
     */
    AlertDialog alertDialog;
    static boolean pullAfterPushInProgress;
    static Handler handler;
    public static Activity progressActivity;
    static boolean isOnPause = true;

    //Check intent params
    static Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        PULL_CANCEL = false;
        PULL_IS_ACTIVE = true;
        isOnPause = false;
        prepareUI();
        final Button button = (Button) findViewById(R.id.cancelPullButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancellPull();
            }
        });

        //the handler and the static activity is needed to show the dialog with the pull
        // controller error.
        handler = new Handler(Looper.getMainLooper());
        progressActivity = this;
        intent = getIntent();
    }

    private void cancellPull() {
        if (PULL_IS_ACTIVE) {
            PULL_CANCEL = true;
            PULL_IS_ACTIVE = false;
            step(getBaseContext().getResources().getString(R.string.cancellingPull));
            if (PullController.getInstance().finishPullJob()) {
                finishAndGo(LoginActivity.class);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isOnPause) {
            launchAction();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (PULL_CANCEL == true) {
            finishAndGo(LoginActivity.class);
        } else if (!hasAPullAfterPush()) {
            finishAndGo(DashboardActivity.class);
        }
        isOnPause = true;
    }

    private void prepareUI() {
        progressBar = (ProgressBar) findViewById(R.id.pull_progress);
        progressBar.setMax(isAPush() ? MAX_PUSH_STEPS : MAX_PULL_STEPS);
        textView = (TextView) findViewById(R.id.pull_text);
    }

    //@Subscribe
    public void onProgressChange(final SyncProgressStatus syncProgressStatus) {
        //bad event or activity not in foreground -> show nothing
        if (syncProgressStatus == null || isOnPause) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (syncProgressStatus.hasError()) {
                    showException(syncProgressStatus.getException().getMessage() + " ");
                    return;
                }

                //Step
                if (syncProgressStatus.hasProgress()) {
                    step(syncProgressStatus.getMessage() + " ");
                    return;
                }

                //Finish
                if (syncProgressStatus.isFinish()) {
                    showAndMoveOn();
                }
            }
        });
    }

    /**
     * Launches a pull or push according to an intent extra
     */
    private void launchAction() {

        //Clear flag
        pullAfterPushInProgress = false;

        //Push or Pull according to extra param from intent
        if (isAPush()) {
            launchPush();
        } else {
            launchPull();
        }
    }

    /**
     * Shows a dialog with the given message y move to login after showing error
     */
    public static void showException(final String msg) {
        final boolean isAPush = isAPush();
        Log.d(TAG, msg + " ");

        PULL_ERROR = true;
        PULL_CANCEL = true;
        PULL_IS_ACTIVE = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Run your task here
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String dialogTitle = "", dialogMessage = "";
                        dialogTitle = getDialogTitle(isAPush());
                        if (msg != null) {
                            dialogMessage = msg;
                        }
                        new AlertDialog.Builder(progressActivity)
                                .setCancelable(false)
                                .setTitle(dialogTitle)
                                .setMessage(dialogMessage)
                                .setNeutralButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                //A crash during a push might be recoverable ->
                                                // dashboard
                                                if (isAPush) {
                                                    Log.d(TAG,
                                                            "Push crashed, moving to dashboard...");
                                                    finishAndGo(DashboardActivity.class);
                                                } else {
                                                    logout();
                                                }
                                            }
                                        }).create().show();
                    }
                });
            }
        });
    }

    private static void logout() {
        Log.d(TAG, "Logging out...");
        UserAccountRepository userAccountRepository = new UserAccountRepository(progressActivity);
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
     * Prints the step in the progress bar
     */
    public static void step(final String msg) {
        final int currentProgress = progressBar.getProgress();
        progressBar.setProgress(currentProgress + 1);
        textView.setText(msg);
    }

    /**
     * Shows a dialog to tell that pull is done and then moves into the dashboard.
     */
    private void showAndMoveOn() {
        if (PULL_ERROR) {
            PULL_ERROR = false;
            return;
        }
        boolean isAPush = isAPush();

        //Annotate pull is done
        if (!isAPush) {
            //If is not active, we need restart the process
            if (!PULL_IS_ACTIVE) {
                SdkController.unregister(this);
                finishAndGo(LoginActivity.class);
                return;
            } else {
                annotateFirstPull(true);
            }
        }

        //Show final step -> done
        step(getString(R.string.progress_pull_done));

        String title = getDialogTitle(isAPush);

        final int msg = getDoneMessage();

        //Show message and go on -> pull or single push = dashboard | push before pull = start pull
        Intent intent = getIntent();
        //Not a pull -> is a Push
        if (intent != null && (intent.getIntExtra(ProgressActivity.AFTER_ACTION,
                ProgressActivity.DONT_SHOW_FEEDBACK) == ProgressActivity.SHOW_FEEDBACK)) {
            this.alertDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //Pull or Push(single)
                            if (!isAPush() || !hasAPullAfterPush()) {
                                finishAndGo(DashboardActivity.class);
                                return;
                            } else {
                                //Start pull after push
                                pullAfterPushInProgress = true;
                                launchPull();
                                return;
                            }
                        }
                    })
                    .setNeutralButton(getApplicationContext().getResources().getString(
                            R.string.dialog_button_preview_feedback),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //I try using a intent to feedbackactivity but the
                                    // dashboardsActivity was reloaded from the service.
                                    finishAndGo(DashboardActivity.class);
                                }
                            }).create();
            alertDialog.show();
        } else {
            this.alertDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(title)
                    .setMessage(msg)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            if (!isAPush() || !hasAPullAfterPush()) {
                                finishAndGo(DashboardActivity.class);
                                return;
                            } else {
                                //Start pull after push
                                pullAfterPushInProgress = true;
                                launchPull();
                                return;
                            }
                        }
                    }).create();
            alertDialog.show();
        }
    }

    /**
     * Once an action is over there is a message that changes depending on the kind of action:
     * -Pull: Pull ok, let's move to dashboard
     * -Push (single): Push ok, let's move to dashboard
     * -Push (before pull): Push ok, let's start with the pull
     */
    private int getDoneMessage() {
        boolean isAPush = isAPush();

        //Pull
        if (!isAPush) {
            return R.string.dialog_pull_success;
        }

        //Push before pull
        if (hasAPullAfterPush()) {
            return R.string.dialog_push_before_pull_success;
        }

        //Push (single)
        return R.string.dialog_push_success;
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

    /**
     * Tells if a push is required
     */
    private static boolean isAPush() {
        //A push before pull
        if (pullAfterPushInProgress) {
            return false;
        }

        //Not a pull -> is a Push
        return (intent != null && intent.getIntExtra(TYPE_OF_ACTION, ACTION_PULL) != ACTION_PULL);
    }


    /**
     * Tells is the intent requires a Pull after the push is done
     */
    private boolean hasAPullAfterPush() {
        Intent intent = getIntent();
        return (intent != null && intent.getIntExtra(TYPE_OF_ACTION, ACTION_PULL)
                == ACTION_PUSH_BEFORE_PULL);
    }

    private static String getDialogTitle(boolean isAPush) {
        int stringId =
                isAPush ? R.string.dialog_title_push_response : R.string.dialog_title_pull_response;
        return PreferencesState.getInstance().getContext().getString(stringId);
    }

    private void launchPull() {
        annotateFirstPull(false);
        progressBar.setProgress(0);
        progressBar.setMax(MAX_PULL_STEPS);
        PullController.getInstance().pull(this);
    }

    /**
     * Launches a push using the PushController according to the intent params
     */
    private void launchPush() {
        annotateFirstPull(true);
        progressBar.setProgress(0);
        progressBar.setMax(MAX_PUSH_STEPS);

        List<Survey> surveys = findSurveysToPush();
        Log.d(TAG, "surveys" + surveys.size());
        PushController.getInstance().push(this, surveys);
    }

    /**
     * Find the surveys that are going to be pushed
     */
    private List<Survey> findSurveysToPush() {
        if (hasAPullAfterPush()) {
            List<Survey> surveys = Survey.getAllUnsentUnplannedSurveys();
            for (int i = 0; i < surveys.size(); i++) {
                if (surveys.get(i).getCompletionDate() == null) {
                    surveys.get(i).setCompleteSurveyState(Constants.PROGRESSACTIVITY_MODULE_KEY);
                }
            }
            return surveys;
        }

        List<Survey> surveys = new ArrayList<>();
        //Fixme it is not used anymore?.
        //surveys.add(Session.getSurveyByModule(module));
        return surveys;
    }

    /**
     * Finish current activity and launches an activity with the given class
     *
     * @param targetActivityClass Given target activity class
     */
    public static void finishAndGo(Class targetActivityClass) {
        Intent targetActivityIntent = new Intent(progressActivity, targetActivityClass);
        progressActivity.finish();
        progressActivity.startActivity(targetActivityIntent);
    }

    public static void cancellPull(final String title, final String errorMessage) {
        PULL_ERROR = true;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Run your task here
                PULL_CANCEL = true;
                PULL_IS_ACTIVE = false;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String dialogTitle = "", dialogMessage = "";
                        if (title != null) {
                            dialogTitle = title;
                        }
                        if (errorMessage != null) {
                            dialogMessage = errorMessage;
                        }
                        try {
                            new AlertDialog.Builder(progressActivity)
                                    .setCancelable(false)
                                    .setTitle(dialogTitle)
                                    .setMessage(dialogMessage)
                                    .setNeutralButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                                    Intent targetActivityIntent = new Intent(
                                                            progressActivity, LoginActivity.class);
                                                    targetActivityIntent.setFlags(
                                                            Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    progressActivity.getApplicationContext()
                                                            .startActivity(

                                                            targetActivityIntent);
                                                    return;
                                                }
                                            }).create().show();
                        } catch (Exception e) {
                            Log.d(TAG, errorMessage);
                        }

                    }
                });
            }
        }, 1000);
    }

    /**
     * Checks if a dialog is being shown (for testing purposes)
     */
    public boolean isDialogShowing() {
        boolean isShowing = this.alertDialog != null && this.alertDialog.isShowing();
        Log.d(TAG, "isDialogShowing ->" + isShowing);
        return isShowing;
    }

    public static void postFinish() {

        ProgressActivity.annotateFirstPull(true);
        finishAndGo(DashboardActivity.class);
        //// FIXME: 28/11/2016 
        //showAndMoveOn();
    }
}
