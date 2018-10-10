package org.eyeseetea.malariacare.factories;

import android.content.Context;
import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.data.boundaries.IDataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IDataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.datasources.ObservationLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.QuestionLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.SurveyLocalDataSource;
import org.eyeseetea.malariacare.data.network.ConnectivityManager;
import org.eyeseetea.malariacare.data.remote.sdk.data.ObservationSDKDhisDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.data.SurveySDKDhisDataSource;
import org.eyeseetea.malariacare.data.repositories.ObservationRepository;
import org.eyeseetea.malariacare.data.repositories.OptionRepository;
import org.eyeseetea.malariacare.data.repositories.OrgUnitRepository;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.data.repositories.SurveyRepository;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;

public class DataFactory extends AFactory {

    @NonNull
    public ISurveyRepository getSurveyRepository() {
        return new SurveyRepository(getSurveyLocalDataSource());
    }

    @NonNull
    public IObservationRepository getObservationRepository() {
        return new ObservationRepository(getObservationLocalDataSource());
    }

    @NonNull
    public IDataLocalDataSource getSurveyDataLocalDataSource() {
        return new SurveyLocalDataSource();
    }

    @NonNull
    public IDataLocalDataSource getObservationDataLocalDataSource() {
        return new ObservationLocalDataSource();
    }

    @NonNull
    public IDataRemoteDataSource getSurveyRemoteDataSource(Context context) {
        IServerMetadataRepository serverMetadataRepository =
                new ServerMetadataRepository(context);
        IOptionRepository optionRepository = new OptionRepository();
        IQuestionRepository questionRepository = new QuestionLocalDataSource();
        IOrgUnitRepository orgUnitRepository = new OrgUnitRepository();

        return new SurveySDKDhisDataSource(context, serverMetadataRepository,
                questionRepository, optionRepository, orgUnitRepository);
    }

    @NonNull
    public IDataRemoteDataSource getObservationRemoteDataSource(Context context) {
        return new ObservationSDKDhisDataSource(context, getSurveyLocalDataSource());
    }

    @NonNull
    private SurveyLocalDataSource getSurveyLocalDataSource() {
        return new SurveyLocalDataSource();
    }

    @NonNull
    private ObservationLocalDataSource getObservationLocalDataSource() {
        return new ObservationLocalDataSource();
    }
}
