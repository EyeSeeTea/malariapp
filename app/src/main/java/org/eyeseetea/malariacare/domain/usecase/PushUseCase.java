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

import static org.eyeseetea.malariacare.domain.entity.UserKt.REQUIRED_AUTHORITY;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushDataController;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerInfoRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.UserFailure;
import org.eyeseetea.malariacare.domain.boundary.repositories.UserRepository;
import org.eyeseetea.malariacare.domain.common.Either;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.User;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.DataToPushNotFoundException;
import org.eyeseetea.malariacare.domain.exception.push.PushReportException;
import org.eyeseetea.malariacare.data.remote.SurveyChecker;

public class PushUseCase implements UseCase {

    private IPushController mPushController;
    private IServerInfoRepository mServerInfoRepository;
    private Credentials credentials;

    private IMainExecutor mMainExecutor;
    private IAsyncExecutor mAsyncExecutor;
    private Callback callback;
    private UserRepository userRepository;


    public PushUseCase(IPushController pushController,
            IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor,
            IServerInfoRepository serverInfoRepository,
            UserRepository userRepository) {
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
        mPushController = pushController;
        mServerInfoRepository = serverInfoRepository;
        this.userRepository = userRepository;
    }

    public void execute(Credentials credentials, final Callback callback) {
        this.credentials = credentials;
        this.callback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        if (mPushController.isPushInProgress()) {
            callback.onPushInProgressError();
            return;
        }
        try {
            if (!isValidServerVersion()) {
                notifyOnServerVersionError();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            notifyOnNetworkError();
        }

        Boolean containsRequiredAuthority = verifyRequiredAuthority();

        if (containsRequiredAuthority) {
            mPushController.changePushInProgress(true);

            SurveyChecker.launchQuarantineChecker();

            mPushController.push(new IPushController.IPushControllerCallback() {
                @Override
                public void onComplete(PushDataController.Kind kind) {
                    System.out.println("PushUseCase Complete");

                    mPushController.changePushInProgress(false);

                    notifyOnComplete(kind);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("PushUseCase error");

                    if (throwable instanceof NetworkException) {
                        mPushController.changePushInProgress(false);
                        notifyOnNetworkError();
                    } else if (throwable instanceof ConversionException) {
                        mPushController.changePushInProgress(false);
                        notifyOnConversionError();
                    } else if (throwable instanceof DataToPushNotFoundException) {
                        mPushController.changePushInProgress(false);
                        notifyOnSurveysNotFoundError();
                    } else if (throwable instanceof PushReportException) {
                        mPushController.changePushInProgress(false);
                        notifyOnPushError();
                    } else {
                        mPushController.changePushInProgress(false);
                        notifyOnPushError();
                    }
                }

                @Override
                public void onInformativeError(Throwable throwable) {
                    notifyOnInformativeError(throwable);
                }
            });
        }
    }

    private Boolean verifyRequiredAuthority() {
        if (!PreferencesState.getInstance().getCreedentials().isDemoCredentials()) {
            Either<UserFailure, User> userResponse = userRepository.getCurrent();

            if (userResponse.isLeft()) {
                notifyOnNetworkError();
                return false;
            } else {
                User user = ((Either.Right<User>) userResponse).getValue();

                if (user.getAuthorities().contains(REQUIRED_AUTHORITY)) {
                    return true;
                } else {
                    notifyRequiredAuthorityError(REQUIRED_AUTHORITY);
                    return false;
                }
            }
        } else {
            return true;
        }
    }


    private boolean isValidServerVersion() throws Exception {
        if (credentials.isDemoCredentials()) {
            return true;
        }
        ServerInfo localServerInfo = mServerInfoRepository.getServerInfo(ReadPolicy.CACHE);

        ServerInfo remoteServerInfo = mServerInfoRepository.getServerInfo(ReadPolicy.NETWORK_FIRST);

        if (localServerInfo.getVersion() == -1 ||
                localServerInfo.getVersion() == remoteServerInfo.getVersion()) {
            return true;
        } else {
            remoteServerInfo.markAsUnsupported();
            mServerInfoRepository.save(remoteServerInfo);
            return false;
        }
    }

    private void notifyRequiredAuthorityError(String RequiredAuthority) {
        mMainExecutor.run(() -> callback.onRequiredAuthorityError(RequiredAuthority));
    }

    private void notifyOnComplete(final PushDataController.Kind kind) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onComplete(kind);
            }
        });
    }

    private void notifyOnPushError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onPushError();
            }
        });
    }

    private void notifyOnSurveysNotFoundError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onSurveysNotFoundError();
            }
        });
    }

    private void notifyOnConversionError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onConversionError();
            }
        });
    }

    private void notifyOnNetworkError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onNetworkError();
            }
        });
    }

    private void notifyOnInformativeError(final Throwable throwable) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onInformativeError(throwable.getMessage());
            }
        });
    }

    private void notifyOnServerVersionError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onServerVersionError();
            }
        });
    }

    public interface Callback {
        void onComplete(PushDataController.Kind kind);

        void onPushError();

        void onPushInProgressError();

        void onSurveysNotFoundError();

        void onInformativeError(String message);

        void onConversionError();

        void onNetworkError();

        void onServerVersionError();

        void onRequiredAuthorityError(String authority);
    }
}
