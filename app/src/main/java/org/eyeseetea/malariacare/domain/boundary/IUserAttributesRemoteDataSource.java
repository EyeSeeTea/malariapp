package org.eyeseetea.malariacare.domain.boundary;

import org.eyeseetea.malariacare.domain.entity.UserAccount;

public interface IUserRemoteDataSource {
    UserAccount getUser(UserAccount user);
}
