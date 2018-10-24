package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.data.filters.UserFilter;
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public class UserAccountRepository implements IUserAccountRepository {

    private final IUserAccountDataSource userAccountAPIDataSource;
    private final IUserAccountDataSource userAccountLocalDataSource;

    public UserAccountRepository(IUserAccountDataSource userAccountAPIDataSource,
                                 IUserAccountDataSource userAccountLocalDataSource){
        this.userAccountAPIDataSource = userAccountAPIDataSource;
        this.userAccountLocalDataSource = userAccountLocalDataSource;
    }

    @Override
    public UserAccount getUser(ReadPolicy readPolicy) throws Exception {

        UserAccount localUserAccount = null;

        UserFilter userFilter = new UserFilter();
        localUserAccount = userAccountLocalDataSource.getUser(userFilter);

        if(readPolicy.equals(ReadPolicy.NETWORK_FIRST)){
            UserAccount userAccount = null;
            try {
                userFilter = new UserFilter();
                userFilter.setUId(localUserAccount.getUserUid());
                userAccount = userAccountAPIDataSource.getUser(userFilter);
                localUserAccount.changeClosedDate(userAccount.getClosedDate());
                localUserAccount.changeAnnouncement(userAccount.getAnnouncement());
                saveUser(localUserAccount);
            } catch (Exception e) {
                return localUserAccount;
            }
        }
        return localUserAccount;
    }

    @Override
    public void saveUser(UserAccount user) {
        userAccountLocalDataSource.saveUser(user);
    }
}
