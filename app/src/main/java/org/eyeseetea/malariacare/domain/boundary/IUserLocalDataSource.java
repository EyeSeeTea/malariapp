package org.eyeseetea.malariacare.domain.boundary;

import org.eyeseetea.malariacare.domain.entity.UserAccount;

public interface IUserLocalDataSource {
    UserAccount getUser(String uid);
    UserAccount getLoggedUser();
    void saveUser(UserAccount user);
}
