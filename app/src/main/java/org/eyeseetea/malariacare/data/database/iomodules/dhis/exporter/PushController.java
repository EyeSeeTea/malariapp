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

import com.raizlabs.android.dbflow.sql.language.Delete;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlan;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.PushDhisSDKDataSource;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.SurveysToPushNotFoundException;
import org.eyeseetea.malariacare.domain.exception.push.PushDhisException;
import org.eyeseetea.malariacare.domain.exception.push.PushReportException;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.models.event.Event;

import java.util.List;
import java.util.Map;

public class PushController implements IPushController {
    private final String TAG = ".PushControllerB&D";

    private Context mContext;
    private PushDhisSDKDataSource mPushDhisSDKDataSource;
    private ConvertToSDKVisitor mConvertToSDKVisitor;

    public enum Kind {EVENTS, PLANS}

    public PushController(Context context) {
        mContext = context;
        mPushDhisSDKDataSource = new PushDhisSDKDataSource();
        mConvertToSDKVisitor = new ConvertToSDKVisitor(mContext);
    }

    public void push(final IPushControllerCallback callback) {

        Log.d(TAG, "push running");
        if (!AUtils.isNetworkAvailable()) {
            Log.d(TAG, "No network");
            callback.onError(new NetworkException());
        } else {

            Log.d(TAG, "Network connected");

            List<Survey> surveys = Survey.getAllCompletedSurveys();

            if (surveys == null || surveys.size() == 0) {
                callback.onError(new SurveysToPushNotFoundException("Null surveys"));
            } else {

                mPushDhisSDKDataSource.wipeEvents();
                Log.d(TAG, "convert surveys to sdk");

                try {
                    convertToSDK(surveys);
                } catch (ConversionException e) {
                    callback.onInformativeError(e);//notify to the user
                    callback.onError(e);//close push
                    return;
                }

                if (EventExtended.getAllEvents().size() == 0) {
                    callback.onError(new ConversionException());
                    return;
                } else {
                    Log.d(TAG, "push data");
                    pushData(callback, Kind.EVENTS);
                }
            }
            List<ObsActionPlan> obsActionPlen =
                    ObsActionPlan.getAllCompletedObsActionPlansInSentSurveys();

            if (obsActionPlen == null || obsActionPlen.size() == 0) {
                callback.onError(new SurveysToPushNotFoundException("Null surveys"));
            } else {

                mPushDhisSDKDataSource.wipeEvents();
                Log.d(TAG, "convert surveys to sdk");

                try {
                    convertObsToSDK(obsActionPlen);
                } catch (ConversionException e) {
                    callback.onInformativeError(e);//notify to the user
                    callback.onError(e);//close push
                    return;
                }

                if (EventExtended.getAllEvents().size() == 0) {
                    callback.onError(new ConversionException());
                    return;
                } else {
                    Log.d(TAG, "push data of observation and plan surveys");
                    pushData(callback, Kind.PLANS);
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

    private void pushData(final IPushControllerCallback callback, final Kind kind) {
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
                            mConvertToSDKVisitor.saveSurveyStatus(mapEventsReports, callback, kind);
                            callback.onComplete();
                        }catch (Exception e){
                            onError(new PushReportException(e));
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if(throwable instanceof  PushReportException
                                || throwable instanceof PushDhisException) {
                            mConvertToSDKVisitor.setSurveysAsQuarantine(kind);
                        }
                        callback.onError(throwable);
                    }
                }, kind);
    }

    private void convertToSDK(List<Survey> surveys) throws ConversionException{
        Log.d(TAG, "Converting APP survey into a SDK event");
        for (Survey survey : surveys) {
            survey.setStatus(Constants.SURVEY_SENDING);
            survey.save();
            survey.accept(mConvertToSDKVisitor);
        }
    }
    private void convertObsToSDK(List<ObsActionPlan> obsActionPlanList) throws ConversionException{
        Log.d(TAG, "Converting APP survey into a SDK event");
        for (ObsActionPlan obsActionPlan : obsActionPlanList) {
            obsActionPlan.setStatus(Constants.SURVEY_SENDING);
            obsActionPlan.save();
            obsActionPlan.accept(mConvertToSDKVisitor);
        }
    }
}
