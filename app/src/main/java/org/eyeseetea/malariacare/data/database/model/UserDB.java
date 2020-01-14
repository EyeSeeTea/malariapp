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

    public static List<UserDB> list() {
        return new Select().from(UserDB.class).queryList();
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
        // for the moment we return just the first entry assuming there will be only one entry,	        List<UserDB> users = new Select().from(UserDB.class)
        // but in the future we will have to tag the logged user  .where(UserDB_Table.username.isNotNull())
        List<UserDB> users = new Select().from(UserDB.class).queryList();
        if (users != null && users.size() != 0) {
            return users.get(0);
        }
        return null;
    }

    public static UserDB getUserByUId( String uid) {
        return new Select().from(UserDB.class).where(UserDB_Table.uid_user.eq(uid)).querySingle();
    }

    public static UserDB searchUser(String value) {
        return new Select()
                .from(UserDB.class)
                .where(UserDB_Table.uid_user.eq(value))
                .or(UserDB_Table.name.eq(value))
                .or(UserDB_Table.username.eq(value))
                .querySingle();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDB user = (UserDB) o;

        if (id_user != user.id_user) return false;
        if (uid_user != null ? !uid_user.equals(user.uid_user) : user.uid_user != null)
            return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (username != null ? !username.equals(user.username) : user.username != null)
            return false;
        if (announcement != null ? !announcement.equals(user.announcement)
                : user.announcement != null) {
            return false;
        }
        if (close_date != null ? !close_date.equals(user.close_date) : user.close_date != null) {
            return false;
        }
        return last_updated != null ? last_updated.equals(user.last_updated)
                : user.last_updated == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id_user ^ (id_user >>> 32));
        result = 31 * result + (uid_user != null ? uid_user.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (announcement != null ? announcement.hashCode() : 0);
        result = 31 * result + (close_date != null ? close_date.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id_user=" + id_user +
                ", uid_user='" + uid_user + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", announcement='" + announcement + '\'' +
                ", close_date=" + close_date +
                ", last_updated=" + last_updated +
                ", surveys=" + surveys +
                '}';
    }
}
