package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IMetadataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionSetRepository;
import org.eyeseetea.malariacare.domain.entity.OptionSet;

import java.util.List;

public class OptionSetRepository
        extends AMetadataRepository<OptionSet>
        implements IOptionSetRepository {

    IMetadataLocalDataSource mOptionSetLocalDataSource;
    IMetadataRemoteDataSource mOptionSetRemoteDataSource;

    public OptionSetRepository(
            IMetadataLocalDataSource orgUnitLevelLocalDataSource,
            IMetadataRemoteDataSource orgUnitLevelRemoteDataSource){
        mOptionSetLocalDataSource = orgUnitLevelLocalDataSource;
        mOptionSetRemoteDataSource = orgUnitLevelRemoteDataSource;
    }

    @Override
    protected List<OptionSet> getAllFromCache() throws Exception {
        return (List<OptionSet>) mOptionSetLocalDataSource.getAll();
    }

    @Override
    protected List<OptionSet> getAllFromNetworkFirst() throws Exception {
        List<OptionSet> remoteOptionSets = (List<OptionSet>)
                mOptionSetRemoteDataSource.getAll();

        mOptionSetLocalDataSource.clearAndSave(remoteOptionSets);

        return remoteOptionSets;
    }
}
