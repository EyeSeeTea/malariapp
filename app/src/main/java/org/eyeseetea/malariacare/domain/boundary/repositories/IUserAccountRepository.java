package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public interface IUserAccountRepository {
    UserAccount getUser(ReadPolicy readPolicy) throws Exception;
    void saveUser(UserAccount user) throws Exception;
}
