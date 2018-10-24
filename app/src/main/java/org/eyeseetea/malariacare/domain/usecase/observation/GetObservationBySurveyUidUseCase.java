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

package org.eyeseetea.malariacare.domain.usecase.observation;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.exception.ObservationNotFoundException;
import org.eyeseetea.malariacare.domain.usecase.UseCase;

public class GetObservationBySurveyUidUseCase implements UseCase {

    public interface Callback {
        void onSuccess(Observation observation);
        void onObservationNotFound();
        void onError(Exception e);
    }

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private final IObservationRepository mObservationRepository;

    private String surveyUid;
    private Callback mCallback;

    public GetObservationBySurveyUidUseCase(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IObservationRepository observationRepository) {
        this.mAsyncExecutor = asyncExecutor;
        this.mMainExecutor = mainExecutor;
        this.mObservationRepository = observationRepository;
    }

    public void execute(String surveyUid, final Callback callback) {
        this.mCallback = callback;
        this.surveyUid = surveyUid;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        Observation observation = null;
        try {
            observation = mObservationRepository.getObservation(this.surveyUid);
            notifyOnComplete(observation);
        } catch (ObservationNotFoundException e){
            notifyNotFoundError();
        }  catch (Exception e){
            notifyError(e);
        }
    }

    private void notifyOnComplete(final Observation observation) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess(observation);
            }
        });
    }

    private void notifyNotFoundError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onObservationNotFound();
            }
        });
    }

    private void notifyError(final Exception e) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onError(e);
            }
        });
    }
}
