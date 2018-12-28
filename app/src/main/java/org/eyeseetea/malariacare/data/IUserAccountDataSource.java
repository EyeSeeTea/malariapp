package org.eyeseetea.malariacare.data;

import org.eyeseetea.malariacare.domain.entity.UserAccount;

public interface IUserAccountDataSource {
    UserAccount getUser() throws Exception;
    void saveUser(UserAccount user) throws Exception;
}
