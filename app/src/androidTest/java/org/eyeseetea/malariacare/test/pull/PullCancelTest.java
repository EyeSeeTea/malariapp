package org.eyeseetea.malariacare.test.pull;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_STAGING;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.cancellPull;

/**
 * Created by idelcano on 8/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class PullCancelTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @BeforeClass
    public static void setupClass(){
        PopulateDB.wipeDatabase();
    }

    @Test
    public void pullCancelledReturnsLogin() {
        login(HNQIS_DEV_STAGING, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
        cancellPull(1);
        assertEquals(LoginActivity.class, SDKTestUtils.getActivityInstance().getClass());
    }
}
