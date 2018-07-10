package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.enums.NetworkStrategy;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public interface IUserAccountRepository {
    UserAccount getUser(NetworkStrategy networkStrategy) throws Exception;
    void saveUser(UserAccount user);
}
