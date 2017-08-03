/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.testSuvreillance.Survey;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.eyeseetea.malariacare.testSuvreillance.Survey.utils.SDKVariantUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_CI;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;

/**
 * Created by idelcano on 21/03/2016.
 */
@RunWith(AndroidJUnit4.class)
public class NoTestSurveyTest {

    private static final String TAG="AssessActionTest";

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

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

    @Test
    public void suvreillanceSurveyNoTest(){
        //GIVEN
        login(HNQIS_DEV_CI, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
        SDKVariantUtils.selectSurvey(1, 1, 5);

        //WHEN
        SDKVariantUtils.fillNoTestSurvey(15);
        Long idSurvey = SDKTestUtils.getSurveyId();
        SurveyDB survey= SurveyDB.findById(idSurvey);

        SDKVariantUtils.clickSend(25);
        //THEN
        Log.d(TAG,survey.toString());
        assertTrue(survey.getValues().size()==1);
        assertTrue(survey.getValues().get(0).getValue().equals("Not Tested"));


    }
}
