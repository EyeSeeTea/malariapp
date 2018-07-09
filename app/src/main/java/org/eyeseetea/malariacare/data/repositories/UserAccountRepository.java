package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.data.NetworkStrategy;
import org.eyeseetea.malariacare.domain.boundary.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.usecase.UserFilter;

public class UserAccountRepository implements IUserAccountRepository {

    private final IUserAccountDataSource userAccountAPIDataSource;
    private final IUserAccountDataSource userAccountLocalDataSource;

    public UserAccountRepository(IUserAccountDataSource userAccountAPIDataSource,
                                 IUserAccountDataSource userAccountLocalDataSource){
        this.userAccountAPIDataSource = userAccountAPIDataSource;
        this.userAccountLocalDataSource = userAccountLocalDataSource;
    }

    @Override
    public UserAccount getUser(UserFilter userFilter, NetworkStrategy networkStrategy) {
        UserAccount userAccount = userAccountLocalDataSource.getUser(userFilter);

        if(networkStrategy.equals(NetworkStrategy.NetworkFirst)){

            userFilter = new UserFilter(userAccount.getUserUid(), false);

            UserAccount localUserAccount = userAccount;

            userAccount = userAccountAPIDataSource.getUser(userFilter);

            if(userAccount==null){
                return localUserAccount;
            }

            userAccountLocalDataSource.saveUser(userAccount);
        }
        return userAccount;
    }
}
