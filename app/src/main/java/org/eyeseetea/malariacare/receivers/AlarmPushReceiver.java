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
 *  along with Facility QA Tool App.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.eyeseetea.malariacare.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushDataController;
import org.eyeseetea.malariacare.layout.dashboard.controllers.DashboardController;
import org.eyeseetea.malariacare.layout.dashboard.controllers.ImproveModuleController;
import org.eyeseetea.malariacare.observables.ObservablePush;
import org.eyeseetea.malariacare.services.PushService;

public class AlarmPushReceiver extends BroadcastReceiver {

    public static final String TAG = ".AlarmPushReceiverB&D";
    //TODO: period has to be parameterized
    private static final long SECONDS = 1000;
    private static final long PUSH_FAIL_PERIOD = 300L;
    private static final long PUSH_SUCCESS_PERIOD = 10L;
    private static AlarmPushReceiver instance;
    private static boolean fail;

    public AlarmPushReceiver() {
    }

    public static void setFail(boolean fail) {
        AlarmPushReceiver.fail = fail;
    }


    public static void isDoneSuccess(PushDataController.Kind kind) {
        Log.i(TAG, "isDoneSuccess");
        setFail(false);

        if (DashboardActivity.dashboardActivity != null) {
            DashboardController dashboardController =
                    DashboardActivity.dashboardActivity.dashboardController;

            if (kind.equals(PushDataController.Kind.EVENTS) &&
                    dashboardController.getCurrentModule() instanceof ImproveModuleController) {
                DashboardActivity.dashboardActivity.reloadActiveTab();
            } else {
                ObservablePush.getInstance().pushFinish();
            }
        }
    }

    public static void isDoneFail() {
        Log.i(TAG, "isDoneFail");
        setFail(true);
    }

    public static void isDoneCancel() {
    }


    /**
     * Launches a PushService call if it is not already in progress
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        Log.d(TAG, "onReceive asking for push");

        WorkManager
                .getInstance(context)
                .enqueueUniqueWork(PushService.UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, PushService.Companion.buildWorkRequest());
    }

    public void setPushAlarm(Context context) {
        Log.d(TAG, "setPushAlarm");

        long pushPeriod = (fail) ? PUSH_FAIL_PERIOD : PUSH_SUCCESS_PERIOD;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmPushReceiver.class);
        //Note FLAG_UPDATE_CURRENT
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pushPeriod * SECONDS,
                pi);

    }

    public static void cancelPushAlarm(Context context) {
        Log.d(TAG, "cancelPushAlarm");

        if (context != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmPushReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(sender);
        }
    }
}