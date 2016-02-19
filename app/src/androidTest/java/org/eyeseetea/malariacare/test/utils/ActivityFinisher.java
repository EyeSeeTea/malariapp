/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.test.utils;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public final class ActivityFinisher implements Runnable {

    public static void finishOpenActivities() {
        new Handler(Looper.getMainLooper()).post(new ActivityFinisher());
    }

    private final ActivityLifecycleMonitor activityLifecycleMonitor;

    private ActivityFinisher() {
        this.activityLifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance();
    }

    @Override
    public void run() {
        final List<Activity> activities = new ArrayList<Activity>();

        for (final Stage stage : EnumSet.range(Stage.CREATED, Stage.STOPPED)) {
            activities.addAll(activityLifecycleMonitor.getActivitiesInStage(stage));
        }

        for (final Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}