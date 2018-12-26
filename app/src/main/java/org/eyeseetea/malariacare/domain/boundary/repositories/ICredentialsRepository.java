package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Credentials;

public interface ICredentialsRepository {
    Credentials getCredentials();

    void saveCredentials(Credentials credentials);
}
