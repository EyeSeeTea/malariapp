package org.eyeseetea.malariacare.test.pull;

        import android.support.test.espresso.Espresso;
        import android.support.test.espresso.NoActivityResumedException;
        import android.support.test.rule.ActivityTestRule;
        import android.support.test.runner.AndroidJUnit4;
        import android.util.Log;

        import org.eyeseetea.malariacare.LoginActivity;
        import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
        import org.junit.After;
        import org.junit.AfterClass;
        import org.junit.Rule;
        import org.junit.Test;
        import org.junit.runner.RunWith;

        import static android.support.test.espresso.Espresso.onView;
        import static android.support.test.espresso.action.ViewActions.click;
        import static android.support.test.espresso.matcher.ViewMatchers.withText;
        import static junit.framework.Assert.assertEquals;
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
    private LoginActivity mReceiptCaptureActivity;

    @AfterClass
    public static void tearDown() throws Exception {
        Log.d(TAG, "TEARDOWN");

        goBackN();

        // super.tearDown();
    }

    private static void goBackN() {
        final int N = 10; // how many times to hit back button
        try {
            for (int i = 0; i < N; i++) {
                Espresso.pressBack();
                try {
                    onView(withText(android.R.string.ok)).perform(click());
                } catch (Exception e) {
                }
            }
        } catch (NoActivityResumedException e) {
            Log.e(TAG, "Closed all activities", e);
        }
    }

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @Test
    public void pullWithOutPermissionDoesNotPull() {

        //GIVEN
        login(HNQIS_DEV_STAGING, TEST_USERNAME_NO_PERMISSION, TEST_PASSWORD_NO_PERMISSION, 60);
        //WHEN
        waitForPull(20);
        //THEN
        assertEquals(LoginActivity.class, SDKTestUtils.getActivityInstance().getClass());
    }
}
