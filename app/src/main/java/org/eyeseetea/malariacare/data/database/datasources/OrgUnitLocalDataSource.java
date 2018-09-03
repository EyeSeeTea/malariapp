package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.database.mapper.OrgUnitDBMapper;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.domain.boundary.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.util.List;

public class OrgUnitLocalDataSource implements IOrgUnitRepository {
    @Override
    public List<OrgUnit> getAll() {
        OrgUnitDBMapper orgUnitDBMapper = new OrgUnitDBMapper();
        return orgUnitDBMapper.mapOrgUnits(OrgUnitDB.getAllOrgUnit());
    }
}
