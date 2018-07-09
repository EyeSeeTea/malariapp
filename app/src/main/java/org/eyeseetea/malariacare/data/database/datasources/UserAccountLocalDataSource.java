package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.usecase.UserFilter;

public class UserAccountLocalDataSource implements IUserAccountDataSource {

    @Override
    public UserAccount getUser(UserFilter userFilter) {
        UserDB userDB;
        if(userFilter.getLoggedUser()) {
            userDB = UserDB.getLoggedUser();
        }else{
            userDB = UserDB.getUserByUId(userFilter.getUid());
        }
      return new UserAccount(userDB.getName(), userDB.getUsername(), userDB.getUid(), userDB.getAnnouncement(), userDB.getCloseDate());

    }

    @Override
    public void saveUser(UserAccount user) {
        UserDB userDB = UserDB.getUserByUId(user.getUserUid());
        if(userDB==null){
            userDB = new UserDB(user.getName(), user.getUserName(), user.getUserUid(),
                    user.getAnnouncement(), user.getClosedDate());
        }else{
            userDB.setAnnouncement(user.getAnnouncement());
            userDB.setCloseDate(user.getClosedDate());
            userDB.setName(user.getUserName());
            userDB.setUsername(user.getUserName());
        }
        userDB.save();
    }
}
