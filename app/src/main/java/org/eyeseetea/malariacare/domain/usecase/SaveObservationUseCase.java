package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository;
import org.eyeseetea.malariacare.domain.entity.Observation;

public class SaveObservationUseCase implements UseCase {

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private final IObservationRepository mObservationRepository;

    private Observation mObservation;
    private Callback mCallback;

    public SaveObservationUseCase(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IObservationRepository observationRepository) {
        this.mAsyncExecutor = asyncExecutor;
        this.mMainExecutor = mainExecutor;
        this.mObservationRepository = observationRepository;
    }

    public void execute(Observation observation, final Callback callback) {
        this.mCallback = callback;
        this.mObservation = observation;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        try {
            mObservationRepository.save(mObservation);
        }  catch (Exception e){
            notifyError(e);
        }
        notifyOnSuccess();
    }

    private void notifyOnSuccess() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess();
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
