package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.NetworkStrategy;
import org.eyeseetea.malariacare.data.remote.api.UserAccountAPIRemoteDataSource;
import org.eyeseetea.malariacare.data.database.datasources.UserAccountLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.exception.GetUserAccountException;
import org.eyeseetea.malariacare.domain.usecase.UserFilter;

public class UserAccountRepository implements IUserAccountRepository {
    private final UserAccountAPIRemoteDataSource userAccountAPIDataSource;
    private final UserAccountLocalDataSource userAccountLocalDataSource;

    public UserAccountRepository(UserAccountAPIRemoteDataSource userAccountAPIDataSource,
                                 UserAccountLocalDataSource userAccountLocalDataSource){
        this.userAccountAPIDataSource = userAccountAPIDataSource;
        this.userAccountLocalDataSource = userAccountLocalDataSource;
    }

    @Override
    public UserAccount getUser(UserFilter userFilter, NetworkStrategy networkStrategy) {
        UserAccount userAccount = null;
        userAccount = userAccountLocalDataSource.getUser(userFilter);
        if(networkStrategy.equals(NetworkStrategy.LocalFirst)){
            return userAccount;
        } else if(networkStrategy.equals(NetworkStrategy.NetworkFirst)){
            try {
                userFilter = new UserFilter(userAccount.getUserUid(), false);
                userAccount = userAccountAPIDataSource.getUser(userFilter);
                userAccountLocalDataSource.saveUser(userAccount);
            } catch (GetUserAccountException e) {
                e.printStackTrace();
            }
        }
        return userAccount;
    }
}
