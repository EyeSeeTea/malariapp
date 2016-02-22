package org.eyeseetea.malariacare.test.pull;

        import android.support.test.espresso.Espresso;
        import android.support.test.espresso.NoActivityResumedException;
        import android.support.test.rule.ActivityTestRule;
        import android.support.test.runner.AndroidJUnit4;
        import android.util.Log;

        import org.eyeseetea.malariacare.LoginActivity;
        import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
        import org.junit.AfterClass;
        import org.junit.Before;
        import org.junit.Rule;
        import org.junit.Test;
        import org.junit.runner.RunWith;

        import static android.support.test.espresso.Espresso.onView;
        import static android.support.test.espresso.action.ViewActions.click;
        import static android.support.test.espresso.matcher.ViewMatchers.withText;
        import static junit.framework.Assert.assertEquals;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.DEFAULT_WAIT_FOR_PULL;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_STAGING;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_NO_PERMISSION;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_NO_PERMISSION;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;

/**
 * Created by idelcano on 8/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class PullErrorTest {

    private static final String TAG="TestingPullError";
    //private LoginActivity mReceiptCaptureActivity;

    @Before
    public void setup(){
        //force init go to logging activity.
        SDKTestUtils.goToLogin();
        //set the test limit( and throw exception if the time is exceded)
        SDKTestUtils.setTestTimeoutSeconds(SDKTestUtils.DEFAULT_TEST_TIME_LIMIT);
    }

    @AfterClass
    public static void exitApp() throws Exception {
        SDKTestUtils.exitApp();
    }

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @Test
    public void pullWithOutPermissionDoesNotPull() {

        //GIVEN
        login(HNQIS_DEV_STAGING, TEST_USERNAME_NO_PERMISSION, TEST_PASSWORD_NO_PERMISSION);
        //WHEN
        waitForPull(DEFAULT_WAIT_FOR_PULL);
        //THEN
        assertEquals(LoginActivity.class, SDKTestUtils.getActivityInstance().getClass());
    }
}
