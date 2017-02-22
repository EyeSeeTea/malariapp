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


package org.eyeseetea.malariacare.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushController;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.usecase.PushUseCase;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.strategies.PushServiceStrategy;

public class PushService extends IntentService {
    /**
     * Constant added to the intent in order to reuse the service for different 'methods'
     */
    public static final String SERVICE_METHOD = "serviceMethod";

    /**
     * Name of 'push all pending surveys' action
     */
    public static final String PENDING_SURVEYS_ACTION =
            "org.eyeseetea.malariacare.services.PushService.PENDING_SURVEYS_ACTION";

    public static final String TAG = ".PushService";

    IPushController pushController;
    PushUseCase pushUseCase;

    PushServiceStrategy mPushServiceStrategy = new PushServiceStrategy(this);

    /**
     * Constructor required due to a error message in AndroidManifest.xml if it is not present
     */
    public PushService() {
        super(PushService.class.getSimpleName());
    }

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PushService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Ignore wrong actions
        if (!PENDING_SURVEYS_ACTION.equals(intent.getStringExtra(SERVICE_METHOD))) {
            return;
        }

        mPushServiceStrategy.push(pushUseCase);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        pushController = new PushController(getApplicationContext());
        pushUseCase = new PushUseCase(pushController);
    }

    public void onPushFinished() {
    }
}
