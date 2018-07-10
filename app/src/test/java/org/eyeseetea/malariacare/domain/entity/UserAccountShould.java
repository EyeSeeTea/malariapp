package org.eyeseetea.malariacare.domain.entity;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Calendar;
import java.util.Date;

public class UserAccountShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_a_useraccount_value_with_mandatory_fields(){
        Date date = new Date();
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", "Announcement", date);

        Assert.assertNotNull(userAccount);
        Assert.assertTrue(userAccount.getName().equals("NAME"));
        Assert.assertTrue(userAccount.getUserName().equals("USERNAME"));
        Assert.assertTrue(userAccount.getUserUid().equals("UID"));
        Assert.assertTrue(userAccount.getAnnouncement().equals("Announcement"));
        Assert.assertTrue(userAccount.isAnnouncementAccept()==false);
        Assert.assertTrue(userAccount.getClosedDate().equals(date));
    }

    @Test
    public void return_is_announcement_accept_true_after_accept_announcement(){
        Date date = new Date();
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", "Announcement", date);
        userAccount.acceptAnnouncement();
        Assert.assertTrue(userAccount.isAnnouncementAccept()==true);
    }

    @Test
    public void return_is_announcement_accept_false_after_change_announcement(){
        Date date = new Date();
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", "Announcement", date);
        userAccount.changeAnnouncement("new Announcement");
        Assert.assertTrue(userAccount.isAnnouncementAccept()==false);
    }

    @Test
    public void return_is_announcement_accept_true_after_accept_announcement_and_change_announcement_with_same_string(){
        Date date = new Date();
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", "Announcement", date);
        userAccount.acceptAnnouncement();
        userAccount.changeAnnouncement("Announcement");
        Assert.assertTrue(userAccount.isAnnouncementAccept()==true);
    }

    @Test
    public void return_is_announcement_accept_false_after_accept_announcement_and_change_announcement_started_with_null_announcement(){
        Date date = new Date();
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", null, date);
        userAccount.acceptAnnouncement();
        userAccount.changeAnnouncement("Announcement");
        Assert.assertTrue(userAccount.isAnnouncementAccept()==false);
    }

    @Test
    public void return_is_announcement_accept_false_after_accept_announcement_and_change_announcement_started_with_empty_announcement(){
        Date date = new Date();
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", "", date);
        userAccount.acceptAnnouncement();
        userAccount.changeAnnouncement("Announcement");
        Assert.assertTrue(userAccount.isAnnouncementAccept()==false);
    }

    @Test
    public void return_is_closed_true_after_change_date_to_past_date(){
        LocalDate localDate = new LocalDate();
        localDate.minusDays(1);
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", "", localDate.toDate());
        Assert.assertTrue(userAccount.isClosed()==true);
    }

    @Test
    public void return_is_closed_false_after_create_user_account_without_date(){
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", "", null);
        Assert.assertTrue(userAccount.isClosed()==false);
    }

    @Test
    public void return_is_closed_false_after_change_date_to_actual_date(){
        Date date = new Date();
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", "", date);
        Assert.assertTrue(userAccount.isClosed()==false);
    }

    @Test
    public void throw_exception_when_create_a__useraccount_with_mandatory_fields_without_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("user uid is required");

        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "", "", null);
    }

    @Test
    public void throw_exception_when_create_a__useraccount_with_mandatory_fields_without_name(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("name is required");

        UserAccount userAccount = new UserAccount("", "USERNAME", "UID", "", null);
    }

    @Test
    public void throw_exception_when_create_a__useraccount_with_mandatory_fields_without_username(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("user name is required");

        UserAccount userAccount = new UserAccount("name", "", "UID", "", null);
    }

}
