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

import org.eyeseetea.malariacare.data.boundaries.ISyncDataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.ISyncDataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.ISyncData;
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
    private final ISyncDataLocalDataSource mSurveyLocalDataSource;
    private final ISyncDataRemoteDataSource mSurveyRemoteDataSource;

    private final ISyncDataLocalDataSource mObservationLocalDataSource;
    private final ISyncDataRemoteDataSource mObservationRemoteDataSource;

    public PushDataController(IConnectivityManager connectivityManager,
            ISyncDataLocalDataSource surveyLocalDataSource,
            ISyncDataLocalDataSource observationLocalDataSource,
            ISyncDataRemoteDataSource surveyRemoteDataSource,
            ISyncDataRemoteDataSource observationRemoteDataSource) {
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
            ISyncDataLocalDataSource syncDataLocalDataSource,
            ISyncDataRemoteDataSource syncDataRemoteDataSource,
            IPushControllerCallback callback) throws Exception {

        List<? extends ISyncData> syncDataList = syncDataLocalDataSource.getDataToSync();

        try {
            if (syncDataList.size() == 0) {
                callback.onError(new DataToPushNotFoundException("Not Exists " +
                        dataClass.getSimpleName() + "s to push"));
            }else{
                markAsSending(syncDataList, syncDataLocalDataSource);

                for (ISyncData syncDataItem: syncDataList) {
                    syncDataItem.assignUploadDate(new Date());
                }

                Map<String, PushReport> pushReportMap = syncDataRemoteDataSource.save(syncDataList);

                handlePushReports(pushReportMap, syncDataList, callback, syncDataLocalDataSource);
            }

        } catch (ConversionException e) {
            markDataAsErrorConversionSync(e.getFailedSyncData(),syncDataLocalDataSource);

            callback.onInformativeError(e);
            callback.onError(e);
        } catch (PushReportException | PushDhisException e){
            markDataAsRetry(syncDataList, syncDataLocalDataSource);

            callback.onError(e);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private void handlePushReports(Map<String, PushReport> pushReportMap,
            List<? extends ISyncData> syncData,  IPushControllerCallback callback,
            ISyncDataLocalDataSource syncDataLocalDataSource) {

        if (pushReportMap == null || pushReportMap.size() == 0) {
            callback.onError(new PushReportException("EventReport is null or empty"));
        } else {
            try {
                for (ISyncData syncDataItem: syncData) {
                    PushReport pushReport= pushReportMap.get(syncDataItem.getSurveyUid());

                    if (pushReport == null) {
                        syncDataItem.markAsRetrySync();
                        Log.d(TAG, "Error saving data: report is null in this survey: "
                                + syncDataItem.getSurveyUid());

                    }else {

                        List<PushConflict> pushConflicts = pushReport.getPushConflicts();

                        if (pushConflicts != null && pushConflicts.size() > 0) {
                            syncDataItem.markAsConflict();

                            for (PushConflict pushConflict : pushConflicts) {
                                if (pushConflict.getUid() != null) {
                                    syncDataItem.markValueAsConflict(pushConflict.getUid());

                                    Log.d(TAG, "saveSurveyStatus: PUSH process...PushConflict in "
                                            + pushConflict.getUid() +
                                            " with error " + pushConflict.getValue()
                                            + " . Pushing survey: " + syncDataItem.getSurveyUid());

                                    callback.onInformativeError(
                                            new PushValueException(syncDataItem.getSurveyUid(),
                                                    pushConflict.getUid(), pushConflict.getValue()));
                                }
                            }
                        }else {
                            if (!pushReport.hasPushErrors(syncDataItem instanceof ObservationDB)) {
                                Log.d(TAG, "saveSurveyStatus: report without errors and status ok "
                                        + syncDataItem.getSurveyUid());
                                syncDataItem.markAsSent();
                            }
                        }
                    }

                    syncDataLocalDataSource.save(syncDataItem);
                }

                callback.onComplete();

            } catch (Exception e) {
                callback.onError(new PushReportException(e));
            }
        }
    }

    private void markAsSending(List<? extends ISyncData> syncData,
            ISyncDataLocalDataSource syncDataLocalDataSource) throws Exception {
        for (ISyncData item :syncData) {
            item.markAsSending();
        }

        syncDataLocalDataSource.save(syncData);
    }

    private void markDataAsErrorConversionSync(ISyncData failedSyncData,
            ISyncDataLocalDataSource syncDataLocalDataSource) {
        failedSyncData.markAsErrorConversionSync();
        syncDataLocalDataSource.save(failedSyncData);
    }

    private void markDataAsRetry(List<? extends ISyncData> syncData,
            ISyncDataLocalDataSource syncDataLocalDataSource) throws Exception {
        for (ISyncData item :syncData) {
            item.markAsRetrySync();
        }
        syncDataLocalDataSource.save(syncData);
    }
}
