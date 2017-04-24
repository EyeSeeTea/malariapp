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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter;

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.PushDhisSDKDataSource;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.PushReport;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.PushReportException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.SurveysToPushNotFoundException;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PushController implements IPushController {
    private final String TAG = ".PushControllerB&D";

    private Context mContext;
    private PushDhisSDKDataSource mPushDhisSDKDataSource;
    private ConvertToSDKVisitor mConvertToSDKVisitor;


    public PushController(Context context) {
        mContext = context;
        mPushDhisSDKDataSource = new PushDhisSDKDataSource();
        mConvertToSDKVisitor = new ConvertToSDKVisitor(mContext);
    }

    public void push(final IPushControllerCallback callback) {

        if (!AUtils.isNetworkAvailable()) {
            Log.d(TAG, "No network");
            callback.onError(new NetworkException());
        } else {

            Log.d(TAG, "Network connected");

            List<Survey> surveys = Survey.getAllCompletedSurveys();

            if (surveys == null || surveys.size() == 0) {
                callback.onError(new SurveysToPushNotFoundException());
            } else {

                mPushDhisSDKDataSource.wipeEvents();
                try {
                    convertToSDK(surveys);
                } catch (Exception ex) {
                    callback.onError(new ConversionException(ex));
                }

                if (EventExtended.getAllEvents().size() == 0) {
                    callback.onError(new ConversionException());
                } else {
                    pushData(callback);
                }
            }
        }
    }

    @Override
    public boolean isPushInProgress() {
        return PreferencesState.getInstance().isPushInProgress();
    }

    @Override
    public void changePushInProgress(boolean inProgress) {
        PreferencesState.getInstance().setPushInProgress(inProgress);
    }

    private void pushData(final IPushControllerCallback callback) {
        mPushDhisSDKDataSource.pushData(
                new IDataSourceCallback<Map<String, PushReport>>() {
                    @Override
                    public void onSuccess(
                            Map<String, PushReport> mapEventsReports) {
                        if(mapEventsReports==null || mapEventsReports.size()==0){
                            onError(new PushReportException("EventReport is null or empty"));
                            return;
                        }
                        try {
                            mConvertToSDKVisitor.saveSurveyStatus(mapEventsReports, callback);
                            callback.onComplete();
                        }catch (Exception e){
                            onError(new PushReportException(e));
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if(throwable instanceof  PushReportException) {
                            mConvertToSDKVisitor.setSurveysAsQuarantine();
                        }
                        callback.onError(throwable);
                    }
                });
    }

    private void convertToSDK(List<Survey> surveys) throws Exception {
        Log.d(TAG, "Converting APP survey into a SDK event");
        for (Survey survey : surveys) {
            survey.setStatus(Constants.SURVEY_SENDING);
            survey.save();
            survey.accept(mConvertToSDKVisitor);
        }
    }
}
