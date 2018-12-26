package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.Credentials;

public interface ICredentialsDataSource {
    Credentials getCredentials();
    void saveCredentials(Credentials credentials);
}
