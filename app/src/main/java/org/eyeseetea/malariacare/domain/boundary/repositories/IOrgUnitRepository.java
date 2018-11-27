package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.util.List;

public interface IOrgUnitRepository {
    List<OrgUnit> getAll(ReadPolicy readPolicy);
}
