package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OrgUnitRepository
        implements IOrgUnitRepository{

    private final IMetadataLocalDataSource<OrgUnit> orgUnitLocalDataSource;

    public OrgUnitRepository(IMetadataLocalDataSource<OrgUnit> orgUnitLocalDataSource){
        this.orgUnitLocalDataSource = orgUnitLocalDataSource;
    }

    @Override
    public List<OrgUnit> getAll() {
        return orgUnitLocalDataSource.getAll();
    }

    @NotNull
    @Override
    public OrgUnit getByUid(@NotNull String uid) {
        return orgUnitLocalDataSource.getByUid(uid);
    }
}