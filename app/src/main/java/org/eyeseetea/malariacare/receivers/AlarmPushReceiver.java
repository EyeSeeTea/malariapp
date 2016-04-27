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

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.services.PushService;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.AUtils;

/**
 * Created by rhardjono on 20/09/2015.
 */
public class AlarmPushReceiver extends BroadcastReceiver {

    public static final String TAG = ".AlarmPushReceiver";

    private static AlarmPushReceiver instance;
    private static boolean fail;
    private static boolean inProgress=false;

    //TODO: period has to be parameterized
    private static final long SECONDS = 1000;

    private static final long PUSH_FAIL_PERIOD = 300L;
    private static final long PUSH_SUCCESS_PERIOD = 10L;

    //the constructor should be public becouse is needed in a receiver class.
    public AlarmPushReceiver(){
    }

    /**
     * Singleton constructor
     *
     * @return
     */
    public static synchronized AlarmPushReceiver getInstance(){
        if(instance==null){
            instance=new AlarmPushReceiver();
        }
        return instance;
    }

    public static void setFail(boolean fail) {
        AlarmPushReceiver.fail = fail;
    }


    public static void isDoneSuccess(){
        Log.i(TAG,"isDoneSuccess");
        setFail(false);
        isDone();
        DashboardActivity.reloadDashboard();
    }

    public static void isDoneFail(){
        Log.i(TAG,"isDoneFail");
        setFail(true);
        isDone();
    }
    /**
     * Notifies the alarm that the push attempt is finished
     */
    public static void isDone(){
        AlarmPushReceiver.inProgress=false;
    }

    /**
     * Launches a PushService call if it is not already in progress
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if(inProgress){
            Log.d(TAG, "onReceive but already pushing");
            return;
        }

        Log.d(TAG, "onReceive asking for push");
        inProgress=true;
        Intent pushIntent=new Intent(context, PushService.class);
        pushIntent.putExtra(SurveyService.SERVICE_METHOD, PushService.PENDING_SURVEYS_ACTION);
        context.startService(pushIntent);
    }

    public void setPushAlarm(Context context) {
        Log.d(TAG, "setPushAlarm");
        if (!AUtils.isNetworkAvailable()){
            cancelPushAlarm(PreferencesState.getInstance().getContext());
            return;
        }

        long pushPeriod = (fail) ? PUSH_FAIL_PERIOD : PUSH_SUCCESS_PERIOD;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmPushReceiver.class);
        //Note FLAG_UPDATE_CURRENT
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pushPeriod * SECONDS, pi);

    }

    public void cancelPushAlarm(Context context) {
        Log.d(TAG, "cancelPushAlarm");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmPushReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(sender);
    }

}