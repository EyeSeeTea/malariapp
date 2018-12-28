package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public class UserAccountRepository implements IUserAccountRepository {

    private final IUserAccountDataSource userAccountRemoteDataSource;
    private final IUserAccountDataSource userAccountLocalDataSource;

    public UserAccountRepository(IUserAccountDataSource userAccountRemoteDataSource,
                                 IUserAccountDataSource userAccountLocalDataSource){
        this.userAccountRemoteDataSource = userAccountRemoteDataSource;
        this.userAccountLocalDataSource = userAccountLocalDataSource;
    }

    @Override
    public UserAccount getUser(ReadPolicy policy) throws Exception {
        if (policy == ReadPolicy.CACHE)
            return  getUserFromCache();
        else if (policy == ReadPolicy.NETWORK_FIRST)
            return getUserFromNetworkFirst();
        else
            throw new IllegalArgumentException(
                    "A UserAccount repository does not implement " + policy + " policy.");
    }

    protected UserAccount getUserFromCache() throws Exception {
        return userAccountLocalDataSource.getUser();
    }

    protected UserAccount getUserFromNetworkFirst() throws Exception {

        UserAccount userAccount;

        try {
            userAccount = userAccountRemoteDataSource.getUser();

            userAccountLocalDataSource.saveUser(userAccount);
        } catch (Exception e){
            userAccount = userAccountLocalDataSource.getUser();
        }

        return userAccount;
    }

    @Override
    public void saveUser(UserAccount user) throws Exception {
        userAccountLocalDataSource.saveUser(user);
    }
}
