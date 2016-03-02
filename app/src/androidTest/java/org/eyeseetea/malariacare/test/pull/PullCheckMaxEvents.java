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

package org.eyeseetea.malariacare.test.pull;

        import android.support.test.rule.ActivityTestRule;
        import android.support.test.runner.AndroidJUnit4;

        import org.eyeseetea.malariacare.LoginActivity;
        import org.eyeseetea.malariacare.database.model.OrgUnit;
        import org.eyeseetea.malariacare.database.model.OrgUnitProgramRelation;
        import org.eyeseetea.malariacare.database.model.Program;
        import org.eyeseetea.malariacare.database.utils.PreferencesState;
        import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
        import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
        import org.junit.AfterClass;
        import org.junit.Before;
        import org.junit.Rule;
        import org.junit.Test;
        import org.junit.runner.RunWith;

        import java.util.List;

        import static junit.framework.Assert.assertTrue;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.DEFAULT_WAIT_FOR_PULL;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_CI;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;
        import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;

/**
 * Created by idelcano on 8/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class PullCheckMaxEvents {

    //private LoginActivity mReceiptCaptureActivity;
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
    public void pullCheckEventsLowThanMaxEvents(){

        //GIVEN
        int maxEvents= PreferencesState.getInstance().getMaxEvents();
        login(HNQIS_DEV_CI, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
        waitForPull(DEFAULT_WAIT_FOR_PULL);


        //WHEN

        List<org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit> organisationUnits=SDKTestUtils.getAllSDKOrganisationUnits();
        List<org.hisp.dhis.android.sdk.persistence.models.Program> programs=SDKTestUtils.getAllSDKPrograms();
        List<org.hisp.dhis.android.sdk.persistence.models.Event> events=SDKTestUtils.getAllSDKEvents();


        //THEN
        int allEvents=0;
        for(org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit organisationUnit:organisationUnits){
            for(org.hisp.dhis.android.sdk.persistence.models.Program program:programs){
                int count=0;
                for(org.hisp.dhis.android.sdk.persistence.models.Event event:events){
                    allEvents++;
                    if(event.getProgramId().equals(program.getUid()) && event.getOrganisationUnitId().equals(organisationUnit.getUuid())){
                        assertTrue(count <= maxEvents);
                    }
                }
            }
        }
        //All events was in program && organisation pair
        assertTrue(allEvents==events.size());


    }

}