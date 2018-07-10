package org.eyeseetea.malariacare.data;

import org.eyeseetea.malariacare.data.filters.UserFilter;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public interface IUserAccountDataSource {
    UserAccount getUser(UserFilter userFilter) throws Exception;
    void saveUser(UserAccount user);
}
