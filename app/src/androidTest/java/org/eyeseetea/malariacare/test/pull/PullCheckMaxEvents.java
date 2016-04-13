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
        import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
        import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
        import org.eyeseetea.malariacare.database.model.OrgUnit;
        import org.eyeseetea.malariacare.database.model.OrgUnitProgramRelation;
        import org.eyeseetea.malariacare.database.model.Program;
        import org.eyeseetea.malariacare.database.utils.PreferencesState;
        import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
        import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
        import org.hisp.dhis.android.sdk.persistence.models.Event;
        import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
        import org.junit.AfterClass;
        import org.junit.Before;
        import org.junit.Rule;
        import org.junit.Test;
        import org.junit.runner.RunWith;

        import java.text.ParseException;
        import java.util.Calendar;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        import static junit.framework.Assert.assertTrue;
        import static junit.framework.Assert.fail;
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

        Calendar month = Calendar.getInstance();
        month.add(Calendar.MONTH, -PullController.NUMBER_OF_MONTHS);
        TrackerController.setStartDate(EventExtended.format(month.getTime(), EventExtended.AMERICAN_DATE_FORMAT));

        //WHEN
        waitForPull(DEFAULT_WAIT_FOR_PULL);


        //THEN: Each combination of program/orgunit has less events than the max

        List<org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit> organisationUnits=SDKTestUtils.getAllSDKOrganisationUnits();
        List<org.hisp.dhis.android.sdk.persistence.models.Program> programs=SDKTestUtils.getAllSDKPrograms();
        List<org.hisp.dhis.android.sdk.persistence.models.Event> events=SDKTestUtils.getAllSDKEvents();

        Map<String,Integer> mapNumEventsXPair= new HashMap<>();
        for(Event event:events){
            try {
                Date eventDate=EventExtended.parseDate(event.getEventDate(),EventExtended.DHIS2_DATE_FORMAT);
                //Then event date is after than the start month date
                assertTrue(eventDate.after(month.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String programId=event.getProgramId();
            String organisationUnitId=event.getOrganisationUnitId();
            String pairKey=programId+organisationUnitId;
            //Get current count + increment
            Integer numPair=mapNumEventsXPair.get(pairKey);
            numPair=(numPair==null)?1:numPair++;

            if(numPair>maxEvents){
                fail(String.format("More events %d than expected %d for orgUnit %s and program %s",numPair,maxEvents,organisationUnitId,programId));
            }
        }
    }

}