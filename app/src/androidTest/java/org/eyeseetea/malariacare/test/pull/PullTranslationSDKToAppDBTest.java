package org.eyeseetea.malariacare.test.pull;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.utils.PopulateDB;
import org.eyeseetea.malariacare.test.utils.ElapsedTimeIdlingResource;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.ProgramAttributeValue;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.DEFAULT_WAIT_FOR_PULL;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_CI;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_STAGING;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;

/**
 * Created by idelcano on 8/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class PullTranslationSDKToAppDBTest {
    private static final String TAG="TestingTransaltion";
    private final String ATTRIBUTE_OU_PRODUCTIVITY_VALUES_CODE="OUPV";
    private final String ATTRIBUTE_PROGRAM_PRODUCTIVITY_POSITION_CODE="PPP";
    private final String PROGRAM_UID_WITH_SUPERVISION="wK0958s1bdj";
    private final String ATTRIBUTE_SUPERVISION_CODE="PSupervisor";
    private final String ATTRIBUTE_SUPERVISION_VALUE="Adrian Quintana";
    private final String ATTRIBUTE_SUPERVISION_ID="vInmonKS0rP";
    private final String ATTRIBUTE_OUPV_VALUE="0815789256346";
    private static List<OrgUnit> orgUnitList;
    private static List<Program> programList;
    private static List<org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit> orgUnitSdkList;
    private static List<org.hisp.dhis.android.sdk.persistence.models.Program> programsSdkList;
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
    public void pullTranslationSDKToAppDB(){

        //GIVEN

        login(HNQIS_DEV_CI, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
        waitForPull(DEFAULT_WAIT_FOR_PULL);

        //WHEN
        waitQuerys(20);

        //THEN
        testOrganisationUnitTranslation();


        //Each sdk program generates an app program
        testProgramUIDTranslation();

        //Check supervisor for an expected program
        testProgramSupervisor();

    }

    private void testProgramSupervisor() {
        boolean hasExpectedSupervisor=false;
        org.hisp.dhis.android.sdk.persistence.models.Program supervisedProgram= MetaDataController.getProgram(PROGRAM_UID_WITH_SUPERVISION);
        for(ProgramAttributeValue programAttributeValue:supervisedProgram.getAttributeValues()) {
            if (programAttributeValue.getAttribute().getCode().equals(ATTRIBUTE_SUPERVISION_CODE) && programAttributeValue.getAttribute().getUid().equals(ATTRIBUTE_SUPERVISION_ID)) {
                if(programAttributeValue.getValue().equals(ATTRIBUTE_SUPERVISION_VALUE)){
                    hasExpectedSupervisor=true;
                    break;
                }
            }
        }
        assertTrue("Checking specific program has expected supervisor",hasExpectedSupervisor);
    }

    private void testProgramUIDTranslation() {
        for(org.hisp.dhis.android.sdk.persistence.models.Program programSDK:programsSdkList){
            boolean hasAProgramWithUID=false;
            for(Program program:programList){
                if(programSDK.getUid().equals(program.getUid())){
                    hasAProgramWithUID=true;
                    break;
                }
            }
            assertTrue("Checking each sdk program generates an app program",hasAProgramWithUID);
        }
    }


    private void testOrganisationUnitTranslation() {
        //Get all the organisation units saved in the sdk, and tests if is saved in our DB
        for(org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit organisationUnit:orgUnitSdkList){
            boolean isOrgUnitUid=false;
            boolean isOrgUnitName=false;
            boolean isOrgUnitProductivity=false;
            boolean isProgramListInOrgUnit=false;
            boolean isOrgUnitOrgUnitLevel=false;
            //WHEN
            for(OrgUnit orgUnit:orgUnitList){
                if(organisationUnit.getId().equals(orgUnit.getUid())){
                    isOrgUnitUid=true;
                    if(organisationUnit.getLabel().equals(orgUnit.getName())){
                        isOrgUnitName=true;
                    }
                    List<OrganisationUnitAttributeValue> attributeValues=organisationUnit.getAttributeValues();

                    for(OrganisationUnitAttributeValue organisationUnitAttributeValue:attributeValues) {
                        if (organisationUnitAttributeValue.getAttribute().getCode().equals(ATTRIBUTE_OU_PRODUCTIVITY_VALUES_CODE))
                            if (organisationUnitAttributeValue.getValue().equals(ATTRIBUTE_OUPV_VALUE)) {
                                isOrgUnitProductivity = true;
                            }
                    }

                    for (org.hisp.dhis.android.sdk.persistence.models.Program program : MetaDataController.getProgramsForOrganisationUnit(organisationUnit.getId(), ProgramType.WITHOUT_REGISTRATION)) {
                        boolean isProgramInOrgUnit=false;
                        for(Program orgUnitProgram:orgUnit.getPrograms()){
                            if(program.getUid().equals(orgUnitProgram.getUid())){
                                isProgramInOrgUnit=true;
                            }
                        }
                        if(!isProgramInOrgUnit) {
                            isProgramListInOrgUnit = false;
                            break;
                        }
                        else
                            isProgramListInOrgUnit=true;
                        }
                    }
                }

            //THEN
            assertTrue("Checking organisationUnit uid",isOrgUnitUid);
            assertTrue("Checking organisationUnit name",isOrgUnitName);
//            assertTrue("Checking organisationUnit productivity",isOrgUnitProductivity);
            assertTrue("Checking organisationUnit program",isProgramListInOrgUnit);
            //Fixme the orgunitlevel is not pulled.
            //assertTrue("Checking organisationUnit orgUnitLevel",isOrgUnitOrgUnitLevel);
        }
    }

    public static void waitQuerys(int secs) {
        //then: wait for progressactivity + dialog + ok (to move to dashboard)
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(secs * 1000);
        Espresso.registerIdlingResources(idlingResource);

        orgUnitList = OrgUnit.getAllOrgUnit();

        programList = Program.getAllPrograms();

        orgUnitSdkList= OrganisationUnitExtended.getAllOrganisationUnits();
        programsSdkList= ProgramExtended.getAllPrograms();

        Espresso.unregisterIdlingResources(idlingResource);
    }
}