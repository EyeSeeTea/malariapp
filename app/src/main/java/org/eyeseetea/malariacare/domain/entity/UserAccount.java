/*
 * Copyright (c) 2017.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.utils.DateParser;

import java.util.Date;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class UserAccount {
    private String name;
    private String userName;
    private String userUid;
    private String announcement;
    private Date closedDate;

    public UserAccount(String name, String userName, String userUid, String announcement, Date closedDate) {
        this.name = required(name, "name is required");
        this.userName = required(userName, "user name is required");
        this.userUid = required(userUid, "user uid is required");
        this.announcement = announcement;
        this.closedDate = closedDate;
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public String setAnnouncement(String announcement) {
        return this.announcement = announcement;
    }

    public boolean isClosed() {
        if(closedDate==null) {
            return false;
        }else{
            return closedDate.before(new Date());
        }
    }

    public Date getClosedDate() {
        return closedDate;
    }
}
