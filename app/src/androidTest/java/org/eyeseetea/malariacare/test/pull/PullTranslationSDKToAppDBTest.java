package org.eyeseetea.malariacare.test.pull;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.test.utils.ElapsedTimeIdlingResource;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.ProgramAttributeValue;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;
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

    private final String ATTRIBUTE_PRODUCTIVITY_CODE="OUProductivity";

    private final String ATTRIBUTE_SUPERVISION_CODE="PSupervisor";
    private final String ATTRIBUTE_SUPERVISION_VALUE="Adrian Quintana";
    private final String ATTRIBUTE_SUPERVISION_ID="zG5T2x5Yjrx";
    private static List<OrgUnit> orgUnitList;
    private static List<Program> programList;
    private static List<org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit> orgUnitSdkList;
    private static List<org.hisp.dhis.android.sdk.persistence.models.Program> programsSdkList;
    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @BeforeClass
    public static void setupClass(){
        PopulateDB.wipeDatabase();
    }

    @Before
    public void setup(){
        PopulateDB.wipeDatabase();
    }

    @Test
    public void pullTranslationSDKToAppDB(){

        //GIVEN

        login(HNQIS_DEV_STAGING, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION, 60);
        waitForPull(30);

        //WHEN
        waitQuerys(20);

        //THEN
        testOrganisationUnitTranslation();


        //Loop all the sdk programs and test if is saved in our DB
        for(org.hisp.dhis.android.sdk.persistence.models.Program programSDK:programsSdkList){
            boolean isProgramUid=false;
            boolean isProgramSupervisor=false;
            //WHEN
            for(Program program : programList) {
                List<ProgramAttributeValue> attributeValues=programSDK.getAttributeValues();
                for(ProgramAttributeValue programAttributeValue:attributeValues) {
                    if (programAttributeValue.getAttribute().getCode().equals(ATTRIBUTE_SUPERVISION_CODE) && programAttributeValue.getAttribute().getUid().equals(ATTRIBUTE_SUPERVISION_ID)) {
                        if(programAttributeValue.getValue().equals(ATTRIBUTE_SUPERVISION_VALUE)){
                            isProgramSupervisor=true;
                            //Fixme here we need check if the attribute of the program is translate to our app db. But at this moment is not converted from the sdk.
                        }
                    }
                }
                if(programSDK.getUid().equals(program.getUid())){
                    isProgramUid=true;
                }
            }
            //THEN
            assertTrue("Checking program Supervisor Code",isProgramSupervisor);
            assertTrue("Checking program name",isProgramUid);
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
                        if (organisationUnitAttributeValue.getAttribute().getCode().equals(ATTRIBUTE_PRODUCTIVITY_CODE))
                            if (organisationUnitAttributeValue.getValue().equals(String.valueOf(orgUnit.getProductivity()))) {
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
            assertTrue("Checking organisationUnit productivity",isOrgUnitProductivity);
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

        orgUnitSdkList= SDKTestUtils.getAllSDKOrganisationUnits();
        programsSdkList= SDKTestUtils.getAllSDKPrograms();

        Espresso.unregisterIdlingResources(idlingResource);
    }
}