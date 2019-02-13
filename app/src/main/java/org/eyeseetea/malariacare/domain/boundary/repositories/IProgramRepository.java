package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.Program;

import java.util.List;

public interface IProgramRepository {
    List<Program> getAll(ReadPolicy policy) throws Exception;

    List<Program> getAllByUIds(ReadPolicy policy, List<String> UIds) throws Exception;
}
