package org.eyeseetea.malariacare.test.pull;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.Assert;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_STAGING;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_NO_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_NO_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.forceDisconnection;

/**
 * Created by idelcano on 8/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class PullComunicationErrorTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @BeforeClass
    public static void setupClass(){
        PopulateDB.wipeDatabase();
    }

    @Test
    public void pullDisconnexionReturnsLogin() {

/*        //GIVEN
        SDKTestUtils.setWifiEnabled(true);

        Assert.assertTrue("Is the network disconnected?", SDKTestUtils.networkState());
        //login(HNQIS_DEV_STAGING, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION,10);

        onView(withId(org.hisp.dhis.android.sdk.R.id.server_url)).perform(replaceText(HNQIS_DEV_STAGING));
        onView(withId(org.hisp.dhis.android.sdk.R.id.username)).perform(replaceText("idelcano"));
        onView(withId(org.hisp.dhis.android.sdk.R.id.password)).perform(replaceText("Ignacio2015xx"));
        onView(withId(org.hisp.dhis.android.sdk.R.id.login_button)).perform(click());
        forceDisconnection();
        boolean isAlertShowed=true;

        //the tester should disconect the network.


        //WHEN
        //Fixme this test is waiting the funcionallity:
        try {
            try{
                Thread.sleep(2*5000);
            }catch(Exception ex){
            }
            //When the error contains a especifique string, we can do it:
            //onView(withText("error")).check(matches(isDisplayed()));

            onView(withText(android.R.string.ok)).perform(click());

        }
        catch(Exception e){
            isAlertShowed=false;
        }

        Assert.assertFalse("Is connected?", SDKTestUtils.networkState());
        Log.d("Test", "" + isAlertShowed);

        try{
            Thread.sleep(5000);
        }catch(Exception ex){
        }
        //restore wifi
        SDKTestUtils.setWifiEnabled(true);

        //THEN
        assertEquals(LoginActivity.class, SDKTestUtils.getActivityInstance().getClass());

        try{
            Thread.sleep(5000);
        }catch(Exception ex){
        }*/
    }
}
