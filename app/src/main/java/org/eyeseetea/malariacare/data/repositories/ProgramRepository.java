package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.DhisFilter;
import org.eyeseetea.malariacare.domain.boundary.repositories.IProgramRepository;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.Program;

import java.util.List;

public class ProgramRepository
        extends AMetadataRepository<Program>
        implements IProgramRepository {

    IMetadataLocalDataSource programLocalDataSource;
    IMetadataRemoteDataSource programRemoteDataSource;

    public ProgramRepository(
            IMetadataLocalDataSource programLocalDataSource,
            IMetadataRemoteDataSource programRemoteDataSource){
        this.programLocalDataSource = programLocalDataSource;
        this.programRemoteDataSource = programRemoteDataSource;
    }

    @Override
    protected List<Program> getAllFromCache() throws Exception {
        return programLocalDataSource.getAll();
    }

    @Override
    protected List<Program> getAllFromNetworkFirst() throws Exception {
        List<Program> remoteOptionSets = programRemoteDataSource.getAll(DhisFilter.empty());

        programLocalDataSource.clearAndSave(remoteOptionSets);

        return remoteOptionSets;
    }

    @Override
    public List<Program> getAllByUIds(ReadPolicy policy, List<String> UIds) throws Exception {
        DhisFilter dhisFilter = new DhisFilter(UIds);

        List<Program> remotePrograms = programRemoteDataSource.getAll(dhisFilter);

        programLocalDataSource.clearAndSave(remotePrograms);

        return remotePrograms;
    }
}
