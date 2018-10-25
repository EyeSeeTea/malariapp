package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitLevelRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;

import java.util.List;

public class OptionSetRepository
        extends AMetadataRepository<OrgUnitLevel>
        implements IOrgUnitLevelRepository {

    IMetadataLocalDataSource mOptionSetLocalDataSource;
    IMetadataRemoteDataSource mOptionSetRemoteDataSource;

    public OptionSetRepository(
            IMetadataLocalDataSource orgUnitLevelLocalDataSource,
            IMetadataRemoteDataSource orgUnitLevelRemoteDataSource){
        mOptionSetLocalDataSource = orgUnitLevelLocalDataSource;
        mOptionSetRemoteDataSource = orgUnitLevelRemoteDataSource;
    }

    @Override
    protected List<OrgUnitLevel> getAllFromCache() throws Exception {
        return (List<OrgUnitLevel>) mOptionSetLocalDataSource.getAll();
    }

    @Override
    protected List<OrgUnitLevel> getAllFromNetworkFirst() throws Exception {
        List<OrgUnitLevel> remoteOrgUnitLevels = (List<OrgUnitLevel>)
                mOptionSetRemoteDataSource.getAll();

        mOptionSetLocalDataSource.clearAndSave(remoteOrgUnitLevels);

        return remoteOrgUnitLevels;
    }
}
