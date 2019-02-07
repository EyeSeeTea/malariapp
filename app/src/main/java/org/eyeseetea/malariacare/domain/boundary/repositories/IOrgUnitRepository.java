package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.util.List;

public interface IOrgUnitRepository {
    List<OrgUnit> getAll(ReadPolicy policy) throws Exception;
    List<OrgUnit> getAllByUIds(ReadPolicy policy, List<String> UIds) throws Exception;
}
