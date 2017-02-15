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

package org.eyeseetea.malariacare.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;

import java.util.Date;
import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class User extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_user;
    @Column
    String uid;
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
    List<Survey> surveys;

    public User() {
    }

    public User(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public Long getId_user() {
        return id_user;
    }

    public void setId_user(Long id_user) {
        this.id_user = id_user;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Survey> getSurveys(){
        if(surveys==null){
            surveys = new Select()
                    .from(Survey.class)
                    .where(Condition.column(Survey$Table.ID_USER)
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

    public static User getLoggedUser(){
        // for the moment we return just the first entry assuming there will be only one entry,but in the future we will have to tag the logged user
        List<User> users = new Select().all().from(User.class).queryList();
        if (users != null && users.size() != 0)
            return users.get(0);
        return null;
    }

    public static User getUser(String value) {
        return new Select()
                .from(User.class)
                .where(Condition.column(Score$Table.UID).eq(value)).querySingle();
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

        User user = (User) o;

        if (id_user != user.id_user) return false;
        if (uid != null ? !uid.equals(user.uid) : user.uid != null) return false;
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
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (announcement != null ? announcement.hashCode() : 0);
        result = 31 * result + (close_date != null ? close_date.hashCode() : 0);
        result = 31 * result + (last_updated != null ? last_updated.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "last_updated=" + last_updated +
                ", id_user=" + id_user +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", announcement='" + announcement + '\'' +
                ", close_date=" + close_date +
                '}';
    }
}
