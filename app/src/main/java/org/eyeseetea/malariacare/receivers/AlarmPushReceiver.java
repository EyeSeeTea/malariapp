/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Survelliance App.
 *
 *  QIS Survelliance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Survelliance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Survelliance App.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.eyeseetea.malariacare.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.services.PushService;
import org.eyeseetea.malariacare.services.SurveyService;

/**
 * Created by rhardjono on 20/09/2015.
 */
public class AlarmPushReceiver extends BroadcastReceiver {

    public static final String TAG = ".AlarmPushReceiver";

    //TODO: period has to be parameterized
    private static final long SECONDS = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        Intent pushIntent=new Intent(context, PushService.class);
        pushIntent.putExtra(SurveyService.SERVICE_METHOD, PushService.PENDING_SURVEYS_ACTION);
        context.startService(pushIntent);
    }


    public void setPushAlarm(Context context) {
        Log.d(TAG, "setPushAlarm");

        long pushPeriod = Long.parseLong(context.getString(R.string.PUSH_PERIOD));
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmPushReceiver.class);
        //Note FLAG_UPDATE_CURRENT
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pushPeriod * SECONDS, pi);

        //others modes:
        //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
    }

    public void cancelPushAlarm(Context context) {
        Log.d(TAG, "cancelPushAlarm");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmPushReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(sender);
    }

}