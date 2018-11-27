package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.database.datasources.OrgUnitLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.util.List;


public class OrgUnitRepository implements IOrgUnitRepository{

    OrgUnitLocalDataSource mOrgUnitLocalDataSource;

    public OrgUnitRepository(OrgUnitLocalDataSource orgUnitLocalDataSource){
        mOrgUnitLocalDataSource = orgUnitLocalDataSource;
    }

    @Override
    public List<OrgUnit> getAll(ReadPolicy readPolicy) {
        if(readPolicy.equals(ReadPolicy.CACHE)){
            return mOrgUnitLocalDataSource.getAll();
        } else {
            /// TODO: 27/11/2018 Add NETWORK_FIRST && NETWORK_NO_CACHE
        }
        return null;
    }
}
