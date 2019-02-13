package org.eyeseetea.malariacare.factories;

import android.content.Context;
import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.datasources.OptionSetLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.OrgUnitLevelLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.OrgUnitLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.ProgramLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.QuestionLocalDataSource;
import org.eyeseetea.malariacare.data.database.datasources.UserAccountLocalDataSource;
import org.eyeseetea.malariacare.data.remote.api.UserAccountD2LightSDKDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.dataSources.OptionSetD2LightSDKDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.dataSources.OrgUnitD2LightSDKDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.dataSources.OrgUnitLevelD2LightSDKDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.dataSources.ProgramD2LightSDKDataSource;
import org.eyeseetea.malariacare.data.repositories.OptionRepository;
import org.eyeseetea.malariacare.data.repositories.OptionSetRepository;
import org.eyeseetea.malariacare.data.repositories.OrgUnitLevelRepository;
import org.eyeseetea.malariacare.data.repositories.OrgUnitRepository;
import org.eyeseetea.malariacare.data.repositories.ProgramRepository;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionSetRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitLevelRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IProgramRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.OptionSet;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;
import org.eyeseetea.malariacare.domain.entity.Program;

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

    public IOrgUnitRepository getOrgUnitRepository(Context context) {
        IMetadataLocalDataSource<OrgUnit> orgUnitLocalDataSource = getOrgUnitLocalDataSource();
        IMetadataRemoteDataSource<OrgUnit> orgUnitRemoteDataSource =
                new OrgUnitD2LightSDKDataSource(context);
        return new OrgUnitRepository(orgUnitLocalDataSource,orgUnitRemoteDataSource);
    }

    @NonNull
    public OrgUnitLocalDataSource getOrgUnitLocalDataSource() {
        return new OrgUnitLocalDataSource();
    }

    public IOrgUnitLevelRepository getOrgUnitLevelRepository(Context context){
        IMetadataLocalDataSource<OrgUnitLevel> orgUnitLocalDataSource =
                new OrgUnitLevelLocalDataSource();
        IMetadataRemoteDataSource <OrgUnitLevel> orgUnitLevelRemoteDataSource =
                new OrgUnitLevelD2LightSDKDataSource(context);

        IOrgUnitLevelRepository orgUnitLevelRepository =
                new OrgUnitLevelRepository(orgUnitLocalDataSource,orgUnitLevelRemoteDataSource);

        return orgUnitLevelRepository;
    }

    public IOptionSetRepository getOptionSetRepository(Context context) {
        IMetadataLocalDataSource<OptionSet> optionSetLocalDataSource =
                new OptionSetLocalDataSource();
        IMetadataRemoteDataSource <OptionSet> optionSetRemoteDataSource =
                new OptionSetD2LightSDKDataSource(context);

        IOptionSetRepository optionSetRepository =
                new OptionSetRepository(optionSetLocalDataSource,optionSetRemoteDataSource);

        return optionSetRepository;
    }

    public IUserAccountRepository getUserAccountRepository(Context context) {
        IUserAccountDataSource userAccountLocalDataSource =
                new UserAccountLocalDataSource();
        IUserAccountDataSource userAccountRemoteDataSource =
                new UserAccountD2LightSDKDataSource(context);


        IUserAccountRepository userAccountRepository =
                new UserAccountRepository(userAccountLocalDataSource, userAccountRemoteDataSource);

        return userAccountRepository;
    }

    public IProgramRepository getProgramRepository(Context context) {
        IMetadataLocalDataSource<Program> programLocalDataSource = new ProgramLocalDataSource();
        IMetadataRemoteDataSource<Program> programRemoteDataSource =
                new ProgramD2LightSDKDataSource(context);
        return new ProgramRepository(programLocalDataSource,programRemoteDataSource);
    }
}
