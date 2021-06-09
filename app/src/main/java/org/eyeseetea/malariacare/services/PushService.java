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

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import org.eyeseetea.malariacare.data.database.datasources.ServerInfoLocalDataSource;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushDataController;
import org.eyeseetea.malariacare.data.remote.api.ServerInfoRemoteDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository;
import org.eyeseetea.malariacare.data.repositories.UserD2ApiRepository;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.UserRepository;
import org.eyeseetea.malariacare.domain.usecase.PushUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.strategies.PushServiceStrategy;

public class PushService extends JobIntentService {
    /**
     * Constant added to the intent in order to reuse the service for different 'methods'
     */
    public static final String SERVICE_METHOD = "serviceMethod";

    /**
     * Name of 'push all pending surveys' action
     */
    public static final String PENDING_SURVEYS_ACTION =
            "org.eyeseetea.malariacare.services.PushService.PENDING_SURVEYS_ACTION";

    public static final String TAG = ".PushServiceB&D";

    IPushController pushController;
    PushUseCase pushUseCase;

    public static final int JOB_ID = 1;

    PushServiceStrategy mPushServiceStrategy = new PushServiceStrategy(this);

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, PushService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //Ignore wrong actions
        if (!PENDING_SURVEYS_ACTION.equals(intent.getStringExtra(SERVICE_METHOD))) {
            return;
        }

        mPushServiceStrategy.push(pushUseCase);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        pushController = new PushDataController(getApplicationContext());

        IMainExecutor mainExecutor = new UIThreadExecutor();
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        ServerInfoRemoteDataSource serverInfoRemoteDataSource = new ServerInfoRemoteDataSource(
                this);
        ServerInfoLocalDataSource serverInfoLocalDataSource = new ServerInfoLocalDataSource(this);
        UserRepository userRepository = new UserD2ApiRepository();
        pushUseCase = new PushUseCase(pushController, mainExecutor, asyncExecutor,
                new ServerInfoRepository(serverInfoLocalDataSource, serverInfoRemoteDataSource),
                userRepository);
    }

    public void onPushFinished() {
    }
}
