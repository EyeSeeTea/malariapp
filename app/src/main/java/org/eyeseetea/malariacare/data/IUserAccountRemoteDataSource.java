package org.eyeseetea.malariacare.data;

import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.exception.GetUserAccountException;
import org.eyeseetea.malariacare.domain.usecase.UserFilter;

public interface IUserAccountRemoteDataSource {
    UserAccount getUser(UserFilter userFilter) throws GetUserAccountException;
}
