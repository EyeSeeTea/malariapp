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
import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.sdk.PushDhisSDKDataSource;
import org.eyeseetea.malariacare.data.sync.IData;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.DataToPushNotFoundException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.push.PushDhisException;
import org.eyeseetea.malariacare.domain.exception.push.PushReportException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PushDataController implements IPushController {
    private final String TAG = ".PushDataController";

    private Context mContext;
    private IConnectivityManager mConnectivityManager;
    private final ISurveyDataSource mSurveyLocalDataSource;
    private final ISurveyDataSource mSurveyRemoteDataSource;

    private PushDhisSDKDataSource mPushDhisSDKDataSource;
    private ConvertToSDKVisitor mConvertToSDKVisitor;

    public PushDataController(Context context, IConnectivityManager connectivityManager,
            ISurveyDataSource surveyLocalDataSource,
            ISurveyDataSource surveyRemoteDataSource) {
        mContext = context;
        mConnectivityManager = connectivityManager;
        mSurveyLocalDataSource = surveyLocalDataSource;
        mSurveyRemoteDataSource = surveyRemoteDataSource;


        mPushDhisSDKDataSource = new PushDhisSDKDataSource();
        mConvertToSDKVisitor = new ConvertToSDKVisitor(mContext);
    }

    @Override
    public boolean isPushInProgress() {
        return PreferencesState.getInstance().isPushInProgress();
    }

    @Override
    public void changePushInProgress(boolean inProgress) {
        PreferencesState.getInstance().setPushInProgress(inProgress);
    }

    public void push(final IPushControllerCallback callback) {

        Log.d(TAG, "push running");
        if (!mConnectivityManager.isDeviceOnline()) {
            Log.d(TAG, "No network");
            callback.onError(new NetworkException());
        } else {

            Log.d(TAG, "Network connected");

            oldPush(callback);
        }
    }

    private void oldPush(final IPushControllerCallback callback) {
        List<IData> surveys = new ArrayList<IData>(SurveyDB.getAllCompletedSurveys());

        convertAndPush(callback, surveys, Kind.EVENTS);

        List<IData> observations = new ArrayList<IData>(
                ObservationDB.getAllCompletedObservationsInSentSurveys());

        convertAndPush(callback, observations, Kind.OBSERVATIONS);
    }

    private void convertAndPush(IPushControllerCallback callback, List<IData> dataList, Kind kind) {
        if (dataList == null || dataList.size() == 0) {
            callback.onError(
                    new DataToPushNotFoundException("No Exists data to push of type:" + kind));
        } else {

            mPushDhisSDKDataSource.wipeEvents();
            Log.d(TAG, "convert data to sdk");

            try {
                convertToSDK(dataList);
            } catch (ConversionException e) {
                callback.onInformativeError(e);//notify to the user
                callback.onError(e);//close push
            }

            if (EventExtended.getAllEvents().size() == 0) {
                callback.onError(new ConversionException());
            } else {
                Log.d(TAG, "push data");
                pushData(callback, kind);
            }
        }
    }

    private void pushData(final IPushControllerCallback callback, final Kind kind) {
        mPushDhisSDKDataSource.pushData(
                new IDataSourceCallback<Map<String, PushReport>>() {
                    @Override
                    public void onSuccess(
                            Map<String, PushReport> mapEventsReports) {
                        if (mapEventsReports == null || mapEventsReports.size() == 0) {
                            onError(new PushReportException("EventReport is null or empty"));
                            return;
                        }
                        try {
                            mConvertToSDKVisitor.saveSurveyStatus(mapEventsReports, callback, kind);
                            callback.onComplete();
                        } catch (Exception e) {
                            onError(new PushReportException(e));
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (throwable instanceof PushReportException
                                || throwable instanceof PushDhisException) {
                            mConvertToSDKVisitor.setSurveysAsQuarantine(kind);
                        }
                        callback.onError(throwable);
                    }
                }, kind);
    }

    private void convertToSDK(List<IData> dataList) throws ConversionException {
        Log.d(TAG, "Converting APP survey into a SDK event");
        for (IData data : dataList) {
            data.changeStatusToSending();
            ((VisitableToSDK) data).accept(mConvertToSDKVisitor);
        }
    }
}
