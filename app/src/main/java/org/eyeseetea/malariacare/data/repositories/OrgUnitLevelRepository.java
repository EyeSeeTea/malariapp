package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitLevelRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;

import java.util.List;

public class OrgUnitLevelRepository
        extends AMetadataRepository<OrgUnitLevel>
        implements IOrgUnitLevelRepository {

    IMetadataLocalDataSource mOrgUnitLevelLocalDataSource;
    IMetadataRemoteDataSource mOrgUnitLevelRemoteDataSource;

    public OrgUnitLevelRepository(
            IMetadataLocalDataSource orgUnitLevelLocalDataSource,
            IMetadataRemoteDataSource orgUnitLevelRemoteDataSource){
        mOrgUnitLevelLocalDataSource = orgUnitLevelLocalDataSource;
        mOrgUnitLevelRemoteDataSource = orgUnitLevelRemoteDataSource;
    }

    @Override
    protected List<OrgUnitLevel> getAllFromCache() throws Exception {
        return (List<OrgUnitLevel>) mOrgUnitLevelLocalDataSource.getAll();
    }

    @Override
    protected List<OrgUnitLevel> getAllFromNetworkFirst() throws Exception {
        List<OrgUnitLevel> remoteOrgUnitLevels = (List<OrgUnitLevel>)
                mOrgUnitLevelRemoteDataSource.getAll();

        mOrgUnitLevelLocalDataSource.clearAndSave(remoteOrgUnitLevels);

        return remoteOrgUnitLevels;
    }
}
