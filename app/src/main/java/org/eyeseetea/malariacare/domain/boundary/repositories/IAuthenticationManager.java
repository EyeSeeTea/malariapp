package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.boundary.IRepositoryCallback;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public interface IUserAccountRepository {

    void login(Credentials credentials, IRepositoryCallback<UserAccount> callback);
    void logout(IRepositoryCallback<Void> callback);
}
