package org.eyeseetea.malariacare.factories;

import android.content.Context;
import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.data.boundaries.IDataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IDataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.MetadataValidator;
import org.eyeseetea.malariacare.data.database.datasources.ObservationLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.SurveyLocalDataSource;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushDataController;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullDataController;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullMetadataController;
import org.eyeseetea.malariacare.data.network.ConnectivityManager;
import org.eyeseetea.malariacare.data.remote.sdk.dataSources.ObservationSDKDhisDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.dataSources.SurveySDKDhisDataSource;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitLevelRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.usecase.PushUseCase;
import org.eyeseetea.malariacare.domain.usecase.pull.PullUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;

public class SyncFactory {
    private IAsyncExecutor asyncExecutor = new AsyncExecutor();
    private IMainExecutor mainExecutor = new UIThreadExecutor();
    private MetadataFactory metadataFactory = new MetadataFactory();

    public PullUseCase getPullUseCase(Context context){
        PullMetadataController pullMetadataController = getPullMetadataController();

        IDataRemoteDataSource surveyRemoteDataSource = getSurveyRemoteDataSource(context);
        IDataLocalDataSource surveyLocalDataSource = getSurveyLocalDataSource();

        PullDataController pullDataController =
                new PullDataController(surveyLocalDataSource, surveyRemoteDataSource);

        MetadataValidator metadataValidator = new MetadataValidator();
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();

        PullUseCase pullUseCase = new PullUseCase(
                asyncExecutor, mainExecutor, pullMetadataController,
                pullDataController, metadataValidator);

        return pullUseCase;
    }

    @NonNull
    private PullMetadataController getPullMetadataController() {
        IOrgUnitLevelRepository orgUnitLevelRepository =
                metadataFactory.getOrgUnitLevelRepository();

        return new PullMetadataController(orgUnitLevelRepository);
    }

    public PushUseCase getPushUseCase(Context context){
        IConnectivityManager connectivityManager = new ConnectivityManager();
        IDataRemoteDataSource surveyRemoteDataSource = getSurveyRemoteDataSource(context);
        IDataLocalDataSource surveyLocalDataSource = getSurveyLocalDataSource();

        IDataLocalDataSource observationLocalDataSource = getObservationLocalDataSource();
        IDataRemoteDataSource observationRemoteDataSource =
                getObservationRemoteDataSource(context);

        IPushController pushController =
                new PushDataController(connectivityManager,
                        surveyLocalDataSource, observationLocalDataSource,
                        surveyRemoteDataSource, observationRemoteDataSource);

        PushUseCase pushUseCase = new PushUseCase(asyncExecutor, mainExecutor, pushController);

        return pushUseCase;
    }

    @NonNull
    private IDataLocalDataSource getSurveyLocalDataSource() {
        return new SurveyLocalDataSource();
    }

    @NonNull
    private IDataRemoteDataSource getSurveyRemoteDataSource(Context context) {

        IServerMetadataRepository serverMetadataRepository =
                metadataFactory.getServerMetadataRepository(context);

        IOptionRepository optionRepository = metadataFactory.getOptionRepository();
        IQuestionRepository questionRepository = metadataFactory.getQuestionLocalDataSource();
        IOrgUnitRepository orgUnitRepository = metadataFactory.getOrgUnitRepository();
        IConnectivityManager connectivityManager = new ConnectivityManager();

        return new SurveySDKDhisDataSource(context, serverMetadataRepository,
                questionRepository, optionRepository, orgUnitRepository, connectivityManager);
    }

    @NonNull
    private IDataLocalDataSource getObservationLocalDataSource() {
        return new ObservationLocalDataSource();
    }

    @NonNull
    private IDataRemoteDataSource getObservationRemoteDataSource(Context context) {
        IServerMetadataRepository serverMetadataRepository =
                metadataFactory.getServerMetadataRepository(context);

        IOptionRepository optionRepository = metadataFactory.getOptionRepository();


        return new ObservationSDKDhisDataSource(context,getSurveyLocalDataSource(),
                serverMetadataRepository, optionRepository);
    }
}
