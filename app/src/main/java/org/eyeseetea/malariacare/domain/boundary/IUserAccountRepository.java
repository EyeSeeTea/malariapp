package org.eyeseetea.malariacare.domain.boundary;

import org.eyeseetea.malariacare.data.NetworkStrategy;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.usecase.UserFilter;

public interface IUserAccountRepository {
    UserAccount getUser(UserFilter userFilter, NetworkStrategy networkStrategy);
}
