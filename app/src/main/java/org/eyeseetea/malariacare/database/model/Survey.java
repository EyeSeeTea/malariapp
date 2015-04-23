/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
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

import com.orm.SugarRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Survey extends SugarRecord<Survey> {

    OrgUnit orgUnit;
    Program program;
    User user;
    Date eventDate;

    public Survey() {
    }

    public Survey(OrgUnit orgUnit, Program program, User user) {
        this.orgUnit = orgUnit;
        this.program = program;
        this.user = user;
        this.eventDate = new Date();
    }

    public Survey(OrgUnit orgUnit, Program program, User user, String eventDate) {
        this.orgUnit = orgUnit;
        this.program = program;
        this.user = user;
        this.eventDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/d");
        try{
            this.eventDate = format.parse(eventDate);
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    public OrgUnit getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit = orgUnit;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public void setEventDate(String eventDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/d");
        try{
            this.eventDate = format.parse(eventDate);
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Survey)) return false;

        Survey survey = (Survey) o;

        if (!eventDate.equals(survey.eventDate)) return false;
        if (!orgUnit.equals(survey.orgUnit)) return false;
        if (!program.equals(survey.program)) return false;
        if (!user.equals(survey.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = orgUnit.hashCode();
        result = 31 * result + program.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + eventDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "orgUnit=" + orgUnit +
                ", program=" + program +
                ", user=" + user +
                ", eventDate='" + eventDate + '\'' +
                '}';
    }
}
