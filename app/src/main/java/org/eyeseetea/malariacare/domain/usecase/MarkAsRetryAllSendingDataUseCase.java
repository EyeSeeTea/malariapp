package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyFilter;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;

import java.util.List;

public class MarkAsRetryAllSendingDataUseCase implements UseCase{
    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private Callback mCallback;
    private ISurveyRepository mSurveyRepository;
    private IObservationRepository mObservationRepository;

    public MarkAsRetryAllSendingDataUseCase(
            IAsyncExecutor asyncExecutor, IMainExecutor mainExecutor,
            ISurveyRepository surveyRepository, IObservationRepository observationRepository) {
        mAsyncExecutor = asyncExecutor;
        mMainExecutor = mainExecutor;
        mSurveyRepository = surveyRepository;
        mObservationRepository = observationRepository;
    }

    public void execute(final Callback callback) {
        this.mCallback = callback;

        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {

        try{
            markAsRetrySendingSurveys();

            markAsRetrySendingObservations();

            notifyOnSuccess();

        } catch (Exception e){
            notifyOnError(e);
        }
    }

    private void markAsRetrySendingSurveys() throws Exception {
        List<Survey> surveys = mSurveyRepository.getSurveys(new SurveyFilter(SurveyStatus.SENDING));

        if (surveys.size()>0) {
            for (Survey survey : surveys) {
                survey.markAsRetrySync();
            }

            mSurveyRepository.save(surveys);
        }
    }

    private void markAsRetrySendingObservations() throws Exception {

        List<Observation> observations =
                mObservationRepository.getObservations(ObservationStatus.SENDING);

        if (observations.size() >0) {
            for (Observation observation : observations) {
                observation.markAsRetrySync();
            }

            mObservationRepository.save(observations);
        }
    }

    private void notifyOnSuccess() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess();
            }
        });
    }

    private void notifyOnError(final Throwable throwable) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onError(throwable.getMessage());
            }
        });
    }
    public interface Callback {
        void onSuccess();
        void onError(String message);
    }
}
