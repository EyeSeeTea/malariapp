package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.ICredentialsDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.ICredentialsRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;

public class CredentialsRepository implements ICredentialsRepository {
    private ICredentialsDataSource mCredentialsDataSource;

    public CredentialsRepository(
            ICredentialsDataSource credentialsDataSource) {
        mCredentialsDataSource = credentialsDataSource;
    }

    @Override
    public Credentials getCredentials() {
        return mCredentialsDataSource.getCredentials();
    }

    @Override
    public void saveCredentials(Credentials credentials) {
        mCredentialsDataSource.saveCredentials(credentials);
    }
}
