package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.OrgUnitLevel;

import java.util.List;

public interface IOrgUnitLevelRepository {
    List<OrgUnitLevel> getAll(ReadPolicy policy) throws Exception;
}
