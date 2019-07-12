package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.util.List;

public class GetOrgUnitsUseCase {
    private IOrgUnitRepository orgUnitRepository;

    public GetOrgUnitsUseCase(IOrgUnitRepository orgUnitRepository){
        this.orgUnitRepository = orgUnitRepository;
    }

    public List<OrgUnit> execute() throws Exception {
        return orgUnitRepository.getAll();
    }
}
