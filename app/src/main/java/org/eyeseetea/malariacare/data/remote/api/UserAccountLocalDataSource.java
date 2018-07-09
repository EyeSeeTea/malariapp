package org.eyeseetea.malariacare.data.remote.api;

import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.domain.boundary.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public class UserAccountRepository implements IUserAccountRepository {

    @Override
    public UserAccount getUser(String uid) {
      UserDB userDB = UserDB.getUserByUId(uid);
      return new UserAccount(userDB.getName(), userDB.getUsername(), userDB.getUid(), userDB.getAnnouncement(), userDB.getCloseDate());
    }

    @Override
    public UserAccount getLoggedUser() {
        UserDB userDB = UserDB.getLoggedUser();
        return new UserAccount(userDB.getName(), userDB.getUsername(), userDB.getUid(),userDB.getAnnouncement(), userDB.getCloseDate());
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
