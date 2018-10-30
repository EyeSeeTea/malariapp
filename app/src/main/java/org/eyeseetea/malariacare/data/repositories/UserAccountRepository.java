package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.dhis2.lightsdk.Dhis2Api;
import org.eyeseetea.dhis2.lightsdk.common.Credentials;
import org.eyeseetea.dhis2.lightsdk.common.Dhis2ApiConfig;
import org.eyeseetea.dhis2.lightsdk.optionsets.OptionSet;
import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.data.filters.UserFilter;
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.enums.NetworkStrategy;

import java.util.List;

public class UserAccountRepository implements IUserAccountRepository {

    private final IUserAccountDataSource userAccountAPIDataSource;
    private final IUserAccountDataSource userAccountLocalDataSource;

    public UserAccountRepository(IUserAccountDataSource userAccountAPIDataSource,
                                 IUserAccountDataSource userAccountLocalDataSource){
        this.userAccountAPIDataSource = userAccountAPIDataSource;
        this.userAccountLocalDataSource = userAccountLocalDataSource;
    }

    @Override
    public UserAccount getUser(NetworkStrategy networkStrategy) throws Exception {

        Dhis2ApiConfig config = new Dhis2ApiConfig("https://dhis2.asia/cnm_android",
                new Credentials("KHMCS","sUcnSA64WhQiUk6A!"));

        Dhis2Api dhis2Api = new Dhis2Api(config);

        List<OptionSet> optionSets = dhis2Api.optionSets().getOptionSets();

        UserAccount localUserAccount = null;

        UserFilter userFilter = new UserFilter();
        localUserAccount = userAccountLocalDataSource.getUser(userFilter);

        if(networkStrategy.equals(NetworkStrategy.NETWORK_FIRST)){
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
