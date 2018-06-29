package org.eyeseetea.malariacare.domain.entity;

import com.google.api.services.drive.model.User;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

public class UserAccountShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_a_useraccount_value_with_mandatory_fields(){
        Date date = new Date();
        UserAttributes userAttributes = new UserAttributes("Announcement", date);
        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "UID", userAttributes);

        Assert.assertNotNull(userAccount);
        Assert.assertTrue(userAccount.getName().equals("NAME"));
        Assert.assertTrue(userAccount.getUserName().equals("USERNAME"));
        Assert.assertTrue(userAccount.getUserUid().equals("UID"));
        Assert.assertTrue(userAccount.getUserAttributes().equals(userAttributes));
        Assert.assertTrue(userAccount.getUserAttributes().getAnnouncement().equals("Announcement"));
        Assert.assertTrue(userAccount.getUserAttributes().getClosedDate().equals(date));
    }

    @Test
    public void throw_exception_when_create_a__useraccount_with_mandatory_fields_without_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("user uid is required");

        UserAccount userAccount = new UserAccount("NAME", "USERNAME", "", UserAttributes.createEmptyUserAttributes());
    }

    @Test
    public void throw_exception_when_create_a__useraccount_with_mandatory_fields_without_name(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("name is required");

        UserAccount userAccount = new UserAccount("", "USERNAME", "UID", UserAttributes.createEmptyUserAttributes());
    }

    @Test
    public void throw_exception_when_create_a__useraccount_with_mandatory_fields_without_username(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("user name is required");

        UserAccount userAccount = new UserAccount("name", "", "UID", UserAttributes.createEmptyUserAttributes());
    }

    @Test
    public void throw_exception_when_create_a__useraccount_with_mandatory_fields_without_userattributes(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("user attributes are required");

        UserAccount userAccount = new UserAccount("name", "username", "UID", null);
    }

}
