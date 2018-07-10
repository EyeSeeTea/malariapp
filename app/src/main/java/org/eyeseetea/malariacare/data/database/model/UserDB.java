/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

import java.util.Date;
import java.util.List;

@Table(database = AppDatabase.class, name = "User")
public class UserDB extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_user;
    @Column
    String uid_user;
    @Column
    String name;
    @Column
    String username;
    @Column
    String announcement;
    @Column
    Date close_date;
    @Column
    Date last_updated;
    @Column
    boolean isAnnouncementAccept;

    /**
     * List of surveys of this user
     */
    List<SurveyDB> surveys;

    public static final String ATTRIBUTE_USER_CLOSE_DATE = "USER_CLOSE_DATE";
    public static final String ATTRIBUTE_USER_ANNOUNCEMENT = "USER_ANNOUNCEMENT";

    public UserDB() {
    }

    public UserDB(String uid, String name) {
        this.uid_user = uid;
        this.name = name;
    }

    public UserDB(String uid, String name, String username, String announcement, Date closeDate, boolean isAnnouncementAccept) {
        this.uid_user = uid;
        this.name = name;
        this.username = username;
        this.announcement = announcement;
        this.close_date = closeDate;
        this.isAnnouncementAccept = isAnnouncementAccept;
    }

    public Long getId_user() {
        return id_user;
    }

    public void setId_user(Long id_user) {
        this.id_user = id_user;
    }

    public String getUid() {
        return uid_user;
    }

    public void setUid(String uid) {
        this.uid_user = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SurveyDB> getSurveys() {
        if (surveys == null) {
            surveys = new Select()
                    .from(SurveyDB.class)
                    .where(SurveyDB_Table.id_user_fk
                            .eq(this.getId_user())).queryList();
        }
        return surveys;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static UserDB getLoggedUser() {
        List<UserDB> users = new Select().from(UserDB.class)
                .where(UserDB_Table.username.isNotNull())
                .queryList();
        if (users != null && users.size() != 0) {
            return users.get(0);
        }
        return null;
    }

    public static UserDB getUserByUId( String uid) {
        return new Select().from(UserDB.class).where(UserDB_Table.uid_user.eq(uid)).querySingle();
    }

    public static UserDB getUser(String value) {
        return new Select()
                .from(UserDB.class)
                .where(UserDB_Table.uid_user.eq(value)).querySingle();
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public Date getCloseDate() {
        return close_date;
    }

    public void setCloseDate(Date close_date) {
        this.close_date = close_date;
    }

    public Date getLastUpdated() {
        return last_updated;
    }

    public void setLastUpdated(Date last_updated) {
        this.last_updated = last_updated;
    }

    public boolean isAnnouncementAccept() {
        return isAnnouncementAccept;
    }

    public void setAnnouncementAccept(boolean announcementAccept) {
        isAnnouncementAccept = announcementAccept;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDB userDB = (UserDB) o;

        if (id_user != userDB.id_user) return false;
        if (isAnnouncementAccept != userDB.isAnnouncementAccept) return false;
        if (uid_user != null ? !uid_user.equals(userDB.uid_user) : userDB.uid_user != null)
            return false;
        if (name != null ? !name.equals(userDB.name) : userDB.name != null) return false;
        if (username != null ? !username.equals(userDB.username) : userDB.username != null)
            return false;
        if (announcement != null ? !announcement.equals(userDB.announcement) : userDB.announcement != null)
            return false;
        if (close_date != null ? !close_date.equals(userDB.close_date) : userDB.close_date != null)
            return false;
        if (last_updated != null ? !last_updated.equals(userDB.last_updated) : userDB.last_updated != null)
            return false;
        return surveys != null ? surveys.equals(userDB.surveys) : userDB.surveys == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id_user ^ (id_user >>> 32));
        result = 31 * result + (uid_user != null ? uid_user.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (announcement != null ? announcement.hashCode() : 0);
        result = 31 * result + (close_date != null ? close_date.hashCode() : 0);
        result = 31 * result + (last_updated != null ? last_updated.hashCode() : 0);
        result = 31 * result + (isAnnouncementAccept ? 1 : 0);
        result = 31 * result + (surveys != null ? surveys.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserDB{" +
                "id_user=" + id_user +
                ", uid_user='" + uid_user + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", announcement='" + announcement + '\'' +
                ", close_date=" + close_date +
                ", last_updated=" + last_updated +
                ", isAnnouncementAccept=" + isAnnouncementAccept +
                ", surveys=" + surveys +
                '}';
    }
}
