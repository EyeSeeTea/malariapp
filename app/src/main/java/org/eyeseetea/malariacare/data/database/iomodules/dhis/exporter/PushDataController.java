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

import android.support.annotation.NonNull;
import android.util.Log;

import org.eyeseetea.malariacare.data.boundaries.IDataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IDataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.IData;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushConflict;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.DataToPushNotFoundException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.push.PushDhisException;
import org.eyeseetea.malariacare.domain.exception.push.PushReportException;
import org.eyeseetea.malariacare.domain.exception.push.PushValueException;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class PushDataController implements IPushController {
    private final String TAG = ".PushDataController";

    private IConnectivityManager mConnectivityManager;
    private final IDataLocalDataSource mSurveyLocalDataSource;
    private final IDataRemoteDataSource mSurveyRemoteDataSource;

    private final IDataLocalDataSource mObservationLocalDataSource;
    private final IDataRemoteDataSource mObservationRemoteDataSource;

    public PushDataController(IConnectivityManager connectivityManager,
            IDataLocalDataSource surveyLocalDataSource,
            IDataLocalDataSource observationLocalDataSource,
            IDataRemoteDataSource surveyRemoteDataSource,
            IDataRemoteDataSource observationRemoteDataSource) {
        mConnectivityManager = connectivityManager;
        mSurveyLocalDataSource = surveyLocalDataSource;
        mSurveyRemoteDataSource = surveyRemoteDataSource;
        mObservationLocalDataSource = observationLocalDataSource;
        mObservationRemoteDataSource = observationRemoteDataSource;
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

            newPush(callback);
        }
    }

    private void newPush(IPushControllerCallback callback){
        try {
            pushData(Survey.class, mSurveyLocalDataSource,
                    mSurveyRemoteDataSource ,callback);

            pushData(Observation.class, mObservationLocalDataSource,
                    mObservationRemoteDataSource ,callback);
        }catch (Exception e){
            callback.onError(e);
        }
    }

    @NonNull
    private void pushData(
            Class<?> dataClass,
            IDataLocalDataSource syncDataLocalDataSource,
            IDataRemoteDataSource syncDataRemoteDataSource,
            IPushControllerCallback callback) throws Exception {

        List<? extends IData> dataList = syncDataLocalDataSource.getDataToSync();

        try {
            if (dataList.size() == 0) {
                callback.onError(new DataToPushNotFoundException("Not Exists " +
                        dataClass.getSimpleName() + "s to push"));
            }else{
                markAsSending(dataList, syncDataLocalDataSource);

                for (IData syncDataItem: dataList) {
                    syncDataItem.assignUploadDate(new Date());
                }

                Map<String, PushReport> pushReportMap = syncDataRemoteDataSource.save(dataList);

                handlePushReports(pushReportMap, dataList, callback, syncDataLocalDataSource);
            }

        } catch (ConversionException e) {
            markDataAsErrorConversionSync(e.getFailedSyncData(),syncDataLocalDataSource);

            callback.onInformativeError(e);
            callback.onError(e);
        } catch (PushReportException | PushDhisException e){
            markDataAsRetry(dataList, syncDataLocalDataSource);

            callback.onError(e);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private void handlePushReports(Map<String, PushReport> pushReportMap,
            List<? extends IData> dataList,  IPushControllerCallback callback,
            IDataLocalDataSource dataLocalDataSource) {

        if (pushReportMap == null || pushReportMap.size() == 0) {
            callback.onError(new PushReportException("EventReport is null or empty"));
        } else {
            try {
                for (IData data: dataList) {
                    PushReport pushReport= pushReportMap.get(data.getSurveyUid());

                    if (pushReport == null) {
                        data.markAsRetrySync();
                        Log.d(TAG, "Error saving data: report is null in this survey: "
                                + data.getSurveyUid());

                    }else {

                        List<PushConflict> pushConflicts = pushReport.getPushConflicts();

                        if (pushConflicts != null && pushConflicts.size() > 0) {
                            data.markAsConflict();

                            for (PushConflict pushConflict : pushConflicts) {
                                if (pushConflict.getUid() != null) {
                                    data.markValueAsConflict(pushConflict.getUid());

                                    Log.d(TAG, "saveSurveyStatus: PUSH process...PushConflict in "
                                            + pushConflict.getUid() +
                                            " with error " + pushConflict.getValue()
                                            + " . Pushing survey: " + data.getSurveyUid());

                                    callback.onInformativeError(
                                            new PushValueException(data.getSurveyUid(),
                                                    pushConflict.getUid(), pushConflict.getValue()));
                                }
                            }
                        }else {
                            if (!pushReport.hasPushErrors(data instanceof Observation)) {
                                Log.d(TAG, "saveSurveyStatus: report without errors and status ok "
                                        + data.getSurveyUid());
                                data.markAsSent();
                            }
                        }
                    }

                    dataLocalDataSource.saveData(data);
                }

                callback.onComplete();

            } catch (Exception e) {
                callback.onError(new PushReportException(e));
            }
        }
    }

    private void markAsSending(List<? extends IData> dataList,
            IDataLocalDataSource dataLocalDataSource) throws Exception {
        for (IData item :dataList) {
            item.markAsSending();
        }

        dataLocalDataSource.saveData(dataList);
    }

    private void markDataAsErrorConversionSync(IData failedData,
            IDataLocalDataSource dataLocalDataSource) {
        failedData.markAsErrorConversionSync();
        dataLocalDataSource.saveData(failedData);
    }

    private void markDataAsRetry(List<? extends IData> dataList,
            IDataLocalDataSource dataLocalDataSource) throws Exception {
        for (IData item :dataList) {
            item.markAsRetrySync();
        }
        dataLocalDataSource.saveData(dataList);
    }
}
