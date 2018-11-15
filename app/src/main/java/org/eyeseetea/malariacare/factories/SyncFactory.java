package org.eyeseetea.malariacare.factories;

import android.content.Context;

import org.eyeseetea.malariacare.data.boundaries.IDataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IDataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.MetadataValidator;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushDataController;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullDataController;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullMetadataController;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.network.ConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.MarkAsRetryAllSendingDataUseCase;
import org.eyeseetea.malariacare.domain.usecase.PushUseCase;
import org.eyeseetea.malariacare.domain.usecase.pull.PullUseCase;

public class SyncFactory extends AFactory{

    private DataFactory mDataFactory;
    private MetadataFactory metadataFactory = new MetadataFactory();

    public SyncFactory(){
        mDataFactory = new DataFactory();
    }

    public PullUseCase getPullUseCase(Context context){
        PullMetadataController pullMetadataController = new PullMetadataController();

        IDataRemoteDataSource surveyRemoteDataSource = mDataFactory.getSurveyRemoteDataSource(context);
        IDataLocalDataSource surveyLocalDataSource = mDataFactory.getSurveyDataLocalDataSource();
        IConnectivityManager connectivityManager = new ConnectivityManager();

        PullDataController pullDataController =
                new PullDataController(surveyLocalDataSource, surveyRemoteDataSource);

        MetadataValidator metadataValidator = new MetadataValidator();

        PullUseCase pullUseCase = new PullUseCase(
                asyncExecutor, mainExecutor, pullMetadataController,
                pullDataController, metadataValidator, connectivityManager);

        return pullUseCase;
    }

    public PushUseCase getPushUseCase(Context context){
        IConnectivityManager connectivityManager = new ConnectivityManager();
        IDataRemoteDataSource surveyRemoteDataSource = mDataFactory.getSurveyRemoteDataSource(context);
        IDataLocalDataSource surveyLocalDataSource = mDataFactory.getSurveyDataLocalDataSource();

        IDataLocalDataSource observationLocalDataSource = mDataFactory.getObservationDataLocalDataSource();
        IDataRemoteDataSource observationRemoteDataSource =
                mDataFactory.getObservationRemoteDataSource(context);

        IPushController pushController =
                new PushDataController(connectivityManager,
                        surveyLocalDataSource, observationLocalDataSource,
                        surveyRemoteDataSource, observationRemoteDataSource);
        PushUseCase pushUseCase = new PushUseCase(asyncExecutor, mainExecutor, pushController, Session.getCredentials(), context);

        return pushUseCase;
    }

    public MarkAsRetryAllSendingDataUseCase getMarkAsRetryAllSendingDataUseCase(){
        ISurveyRepository surveyRepository = mDataFactory.getSurveyRepository();
        IObservationRepository observationRepository = mDataFactory.getObservationRepository();

        MarkAsRetryAllSendingDataUseCase markAsRetryAllSendingDataUseCase =
                new MarkAsRetryAllSendingDataUseCase(asyncExecutor, mainExecutor,
                        surveyRepository, observationRepository);

        return markAsRetryAllSendingDataUseCase;
    }
}
