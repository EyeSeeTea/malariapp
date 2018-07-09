package org.eyeseetea.malariacare.data;

import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.usecase.UserFilter;

public interface IUserAccountDataSource {
    UserAccount getUser(UserFilter userFilter);
    void saveUser(UserAccount user);
}
