package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;

import java.util.List;

public abstract class AMetadataRepository <T>{

    protected abstract List<T> getAllFromCache() throws Exception;
    protected abstract List<T> getAllFromNetworkFirst() throws Exception;

    public List<T> getAll(ReadPolicy policy) throws Exception {
        if (policy == ReadPolicy.CACHE)
            return  getAllFromCache();
        else if (policy == ReadPolicy.NETWORK_FIRST)
            return getAllFromNetworkFirst();
        else
            throw new IllegalArgumentException(
                    "A Metadata repository does not implement " + policy + " policy.");
    }
}
