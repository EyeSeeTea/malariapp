package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IProgramRepository;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProgramRepository implements IProgramRepository {

    IMetadataLocalDataSource programLocalDataSource;

    public ProgramRepository(
            IMetadataLocalDataSource programLocalDataSource){
        this.programLocalDataSource = programLocalDataSource;
    }

    @Override
    public List<Program> getAll(){
        return programLocalDataSource.getAll();
    }

    @NotNull
    @Override
    public Program getByUid(@NotNull String uid) {
        return (Program) programLocalDataSource.getByUid(uid);
    }
}