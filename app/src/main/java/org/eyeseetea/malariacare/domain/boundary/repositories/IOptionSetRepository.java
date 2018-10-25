package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.OptionSet;

import java.util.List;

public interface IOptionSetRepository {
    List<OptionSet> getAll(ReadPolicy policy) throws Exception;
}
