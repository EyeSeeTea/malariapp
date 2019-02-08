package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.DhisFilter;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.util.List;

public class OrgUnitRepository
        extends AMetadataRepository<OrgUnit>
        implements IOrgUnitRepository{

    private final IMetadataLocalDataSource<OrgUnit> orgUnitLocalDataSource;
    private final IMetadataRemoteDataSource<OrgUnit> orgUnitRemoteDataSource;

    public OrgUnitRepository(IMetadataLocalDataSource<OrgUnit> orgUnitLocalDataSource,
            IMetadataRemoteDataSource<OrgUnit> orgUnitRemoteDataSource){
        this.orgUnitLocalDataSource = orgUnitLocalDataSource;
        this.orgUnitRemoteDataSource = orgUnitRemoteDataSource;
    }

    @Override
    protected List<OrgUnit> getAllFromCache() throws Exception {
        return orgUnitLocalDataSource.getAll();
    }

    @Override
    protected List<OrgUnit> getAllFromNetworkFirst() throws Exception {
        List<OrgUnit> remoteOrgUnits = orgUnitRemoteDataSource.getAll(DhisFilter.empty());

        orgUnitLocalDataSource.clearAndSave(remoteOrgUnits);

        return remoteOrgUnits;
    }

    @Override
    public List<OrgUnit> getAllByUIds(ReadPolicy policy, List<String> UIds) throws Exception {
        DhisFilter dhisFilter = new DhisFilter(UIds);

        List<OrgUnit> remoteOrgUnits = orgUnitRemoteDataSource.getAll(dhisFilter);

        orgUnitLocalDataSource.clearAndSave(remoteOrgUnits);

        return remoteOrgUnits;
    }
}
