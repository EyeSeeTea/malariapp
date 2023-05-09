package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitLevelRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;

import java.util.List;

public class OrgUnitLevelRepository
        implements IOrgUnitLevelRepository {

    private final IMetadataLocalDataSource<OrgUnitLevel> orgUnitLocalDataSource;

    public OrgUnitLevelRepository(IMetadataLocalDataSource<OrgUnitLevel> orgUnitLocalDataSource){
        this.orgUnitLocalDataSource = orgUnitLocalDataSource;
    }

    @Override
    public List<OrgUnitLevel> getAll() {
        return orgUnitLocalDataSource.getAll();
    }
}