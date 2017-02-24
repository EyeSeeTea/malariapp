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

import java.util.List;

@Table(database = AppDatabase.class)
public class User extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_user;
    @Column
    String uid_user;
    @Column
    String name;
    @Column
    String username;

    /**
     * List of surveys of this user
     */
    List<Survey> surveys;

    public User() {
    }

    public User(String uid, String name) {
        this.uid_user = uid;
        this.name = name;
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

    public List<Survey> getSurveys(){
        if(surveys==null){
            surveys = new Select()
                    .from(Survey.class)
                    .where(Survey_Table.id_user_fk
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
        List<User> users = new Select().from(User.class).queryList();
        if (users != null && users.size() != 0)
            return users.get(0);
        return null;
    }

    public static User getUser(String value) {
        return new Select()
                .from(User.class)
                .where(User_Table.uid_user.eq(value)).querySingle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (id_user != user.id_user) return false;
        if (uid_user != null ? !uid_user.equals(user.uid_user) : user.uid_user != null) return false;
        return !(name != null ? !name.equals(user.name) : user.name != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_user ^ (id_user >>> 32));
        result = 31 * result + (uid_user != null ? uid_user.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id_user +
                ", uid_user='" + uid_user + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
