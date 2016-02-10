package org.eyeseetea.malariacare.test.pull;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.hisp.dhis.android.sdk.persistence.models.ProgramAttributeValue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_STAGING;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_NO_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_NO_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;

/**
 * Created by idelcano on 8/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class PullOKTest {

    private final String ATTRIBUTE_SUPERVISION_CODE="PSupervisor";
    private final String ATTRIBUTE_SUPERVISION_VALUE="Adrian Quintana";
    private final String ATTRIBUTE_SUPERVISION_ID="zG5T2x5Yjrx";

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
    public void pullWithTestUser(){
        login(HNQIS_DEV_STAGING, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);

        waitForPull(20);

        //Create the Test Program and OrgUnit to compare with real data.
        Program testProgram= new Program("wK0958s1bdj","KE HNQIS Family Planning");
        OrgUnit testOrgUnit= new OrgUnit("QS7sK8XzdQc","KE - HNQIS SF pilot test facility 1",null,null);
        testOrgUnit.setProductivity(10);
        testOrgUnit.addProgram(testProgram);
        //Fixme the orgunitlevel is not pulled.
        //testOrgUnit.setOrgUnitLevel(new OrgUnitLevel("Zone"));

        OrgUnit pulledOrgUnit=SDKTestUtils.getOrgUnit(testOrgUnit.getName());
        assertTrue(testOrgUnit.getName().equals(pulledOrgUnit.getName()));
        assertTrue(testOrgUnit.getUid().equals(pulledOrgUnit.getUid()));
        assertTrue(testOrgUnit.getProductivity().equals(pulledOrgUnit.getProductivity()));
        //Fixme the orgunitlevel is not pulled.
        //assertTrue(testOrgUnit.getOrgUnitLevel().getName().equals(pulledOrgUnit.getOrgUnitLevel().getName()));
        assertTrue(testProgram.getUid().equals(pulledOrgUnit.getPrograms().get(0).getUid()));

        org.hisp.dhis.android.sdk.persistence.models.Program pulledProgram=SDKTestUtils.getProgram(testProgram.getName());

        assertTrue(testProgram.getUid().equals(pulledProgram.getUid()));
        assertTrue(testProgram.getName().equals(pulledProgram.getName()));
        List<ProgramAttributeValue> attributeValues=pulledProgram.getAttributeValues();
        boolean isProductivityCode=false;
        for(ProgramAttributeValue programAttributeValue:attributeValues) {
            if (programAttributeValue.getAttribute().getCode().equals(ATTRIBUTE_SUPERVISION_CODE) && programAttributeValue.getAttribute().getUid().equals(ATTRIBUTE_SUPERVISION_ID)) {
                if(programAttributeValue.getValue().equals(ATTRIBUTE_SUPERVISION_VALUE))
                    isProductivityCode=true;
                //Fixme here we need check if the attribute of the program is translate to our app db. But at this moment is not converted from the sdk.
            }
        }
        assertTrue(isProductivityCode);

    }

}