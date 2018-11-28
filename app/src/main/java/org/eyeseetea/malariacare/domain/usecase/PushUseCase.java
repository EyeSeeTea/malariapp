/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.domain.usecase;

import android.util.Log;

import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.DataToPushNotFoundException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.push.PushReportException;

import java.util.ArrayList;
import java.util.List;

public class PushUseCase implements UseCase{
    private final String TAG = "PushUseCase";

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private IPushController mPushController;
    private final IConnectivityManager mConnectivityManager;
    private final IOrgUnitRepository mOrgUnitRepository;
    private final ISurveyRepository mSurveyRepository;
    private Callback mCallback;

    public PushUseCase(IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IPushController pushController,
            IOrgUnitRepository orgUnitRepository,
            ISurveyRepository surveyRepository,
            IConnectivityManager connectivityManager
                       ) {
        mAsyncExecutor = asyncExecutor;
        mMainExecutor = mainExecutor;
        mPushController = pushController;
        mOrgUnitRepository = orgUnitRepository;
        mSurveyRepository = surveyRepository;
        mConnectivityManager = connectivityManager;
    }

    public void execute(final Callback callback) {
        this.mCallback = callback;

        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        boolean isNetworkAvailable = mConnectivityManager.isDeviceOnline();

        if (isNetworkAvailable) {
            if (mPushController.isPushInProgress()) {
                notifyOnPushInProgressError();
                return;
            }
            mPushController.changePushInProgress(true);


            mAsyncExecutor.run(new Runnable() {
                @Override
                public void run() {
                    launchQuarantineChecker();
                }
            });

            mPushController.push(new IPushController.IPushControllerCallback() {
                @Override
                public void onComplete() {
                    System.out.println("PusUseCase Complete");

                    mPushController.changePushInProgress(false);

                    notifyOnComplete();
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("PusUseCase error");

                    notifyOnError(throwable);
                }

                @Override
                public void onInformativeError(Throwable throwable) {
                    notifyOnInformativeError(throwable);
                }
            });
        }
    }

    private void launchQuarantineChecker() {
        try {
            List<Survey> surveys = mSurveyRepository.getSurveys(SurveyFilter.getQuarantineSurveys());
            if(surveys!=null && surveys.size()>0) {
                List<String> uids = new ArrayList<>();
                for(Survey survey:surveys){
                    uids.add(survey.getUId());
                }
                SurveyFilter surveyFilter = SurveyFilter.getSurveysUidsOnServer(uids);
                List<Survey> quarantineSurveysInServer = mSurveyRepository.getSurveys(surveyFilter);

                updateSurveyStatus(surveys, quarantineSurveysInServer);
                mSurveyRepository.save(surveys);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSurveyStatus(List<Survey> surveys, List<Survey> quarantineSurveysInServer) {
        for(Survey localSurvey : surveys) {
            localSurvey.changeStatus(SurveyStatus.COMPLETED);
            Log.d(TAG, "searching quarantine survey on server, uid: "+localSurvey.getUId());
            for(Survey serverSurvey : quarantineSurveysInServer){
                if(localSurvey.getUId().equals(serverSurvey.getUId())){
                    Log.d(TAG, "quarantine survey is on server, uid: "+localSurvey.getUId());
                    localSurvey.changeStatus(SurveyStatus.SENT);
                }
            }
        }
    }


    private void notifyOnComplete() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onComplete();
            }
        });
    }

    private void notifyOnError(final Throwable throwable) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                if (throwable instanceof NetworkException) {
                    mPushController.changePushInProgress(false);
                    mCallback.onNetworkError();
                } else if (throwable instanceof ConversionException) {
                    mPushController.changePushInProgress(false);
                    mCallback.onConversionError();
                } else if (throwable instanceof DataToPushNotFoundException) {
                    mPushController.changePushInProgress(false);
                    mCallback.onSurveysNotFoundError();
                } else if (throwable instanceof PushReportException){
                    mPushController.changePushInProgress(false);
                    mCallback.onPushError();
                } else {
                    mPushController.changePushInProgress(false);
                    mCallback.onPushError();
                }
            }
        });
    }

    private void notifyOnInformativeError(final Throwable throwable) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onInformativeError(throwable);
            }
        });
    }

    private void notifyOnPushInProgressError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onPushInProgressError();
            }
        });
    }

    public interface Callback {
        void onComplete();

        void onPushError();

        void onPushInProgressError();

        void onSurveysNotFoundError();

        void onInformativeError(Throwable throwable);

        void onConversionError();

        void onNetworkError();
    }
}
