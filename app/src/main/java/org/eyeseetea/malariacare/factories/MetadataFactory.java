package org.eyeseetea.malariacare.factories;

import android.content.Context;
import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.datasources.OrgUnitLevelLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.QuestionLocalDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.dataSources.OrgUnitLevelSDKDhisDataSource;
import org.eyeseetea.malariacare.data.repositories.OptionRepository;
import org.eyeseetea.malariacare.data.repositories.OrgUnitLevelRepository;
import org.eyeseetea.malariacare.data.repositories.OrgUnitRepository;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitLevelRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;

public class MetadataFactory {
    @NonNull
    public IServerMetadataRepository getServerMetadataRepository(Context context) {
        return new ServerMetadataRepository(context);
    }

    @NonNull
    public IOptionRepository getOptionRepository() {
        return new OptionRepository();
    }

    public IQuestionRepository getQuestionLocalDataSource() {
        return new QuestionLocalDataSource();
    }

    public IOrgUnitRepository getOrgUnitRepository() {
        return new OrgUnitRepository();
    }

    public IOrgUnitLevelRepository getOrgUnitLevelRepository(){
        IMetadataLocalDataSource<OrgUnitLevel> orgUnitLocalDataSource =
                new OrgUnitLevelLocalDataSource();
        IMetadataRemoteDataSource <OrgUnitLevel> orgUnitLevelRemoteDataSource =
                new OrgUnitLevelSDKDhisDataSource();

        IOrgUnitLevelRepository orgUnitLevelRepository =
                new OrgUnitLevelRepository(orgUnitLocalDataSource,orgUnitLevelRemoteDataSource);

        return orgUnitLevelRepository;
    }
}
