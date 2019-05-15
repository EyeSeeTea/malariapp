package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.repositories.IProgramRepository;
import org.eyeseetea.malariacare.domain.entity.Program;

import java.util.List;

public class GetProgramsUseCase {
    private IProgramRepository programRepository;

    public GetProgramsUseCase(IProgramRepository programRepository){
        this.programRepository = programRepository;
    }

    List<Program> execute() throws Exception {
        return programRepository.getAll();
    }
}
