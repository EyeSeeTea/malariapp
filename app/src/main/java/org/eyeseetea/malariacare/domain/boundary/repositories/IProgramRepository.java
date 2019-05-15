package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Program;

import java.util.List;

public interface IProgramRepository {
    List<Program> getAll() throws Exception;
}