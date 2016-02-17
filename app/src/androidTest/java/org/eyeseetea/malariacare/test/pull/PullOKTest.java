package org.eyeseetea.malariacare.test.pull;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.hisp.dhis.android.sdk.persistence.models.Access;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitDataSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitGroup;
import org.hisp.dhis.android.sdk.persistence.models.ProgramAttributeValue;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_STAGING;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
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
    private final String PROGRAM_PROGRAMTYPE="without_registration";
    private OrganisationUnit goldenOrganisationUnit;
    private OrgUnit goldenOrgUnit;
    private org.hisp.dhis.android.sdk.persistence.models.Program goldenSdkProgram;
    private Program goldenProgram;
    private List<String> goldenDataSets;
    private List<String> goldenOrganisationUnitGroups;
    
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
    
    @Before
    public void populateTestModels(){
        createRealOrganisationUnit();
        createRealSdkProgram();
        createRealAppProgram();
        createRealOrgUnit();
    }

    @Test
    public void pullWithPermissionDoesNotPull(){

        //GIVEN
        login(HNQIS_DEV_STAGING, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION,60);

        waitForPull(20);

        //WHEN
        //Test organisationUnit has been downloaded with the correct propierties.
        OrganisationUnit sdkOrganisationUnit=SDKTestUtils.getOrganisationUnit(goldenOrganisationUnit.getId());
        //Test orgUnit in app DB has been saved with the correct propierties.
        OrgUnit appOrgUnit=SDKTestUtils.getOrgUnit(goldenOrgUnit.getUid());
        //Test program (in sdk) has been downloaded with the correct propierties.
        org.hisp.dhis.android.sdk.persistence.models.Program sdkProgram=SDKTestUtils.getSDKProgram(goldenProgram.getUid());

        //THEN
        testSdkOrganisationUnit(sdkOrganisationUnit);

        testOrgUnit(appOrgUnit);

        testSdkProgram(sdkProgram);

        testOrgUnitPrograms(sdkProgram);

        testProgramAttribute(sdkProgram);

    }

    public void createRealOrganisationUnit(){
        //Create program and organisationUnit to test the download and saved objects in SDK DB.
        goldenOrganisationUnit =new OrganisationUnit();
        goldenDataSets =new ArrayList<>();
        goldenOrganisationUnitGroups =new ArrayList<>();

        goldenOrganisationUnit.setId("QS7sK8XzdQc");
        goldenOrganisationUnit.setLabel("KE - HNQIS SF pilot test facility 1");
        goldenOrganisationUnit.setLevel(8);
        goldenOrganisationUnit.setParent("spT8zFVQsvx");
        goldenOrganisationUnit.setUuid("68c66a34-806f-49d1-9593-ba5d399ce95e");
        goldenOrganisationUnit.setLastUpdated("2016-01-29T17:47:13.909+0000");
        goldenOrganisationUnit.setCreated("2015-08-06T12:25:04.675+0000");
        goldenOrganisationUnit.setName("KE - HNQIS SF pilot test facility 1");
        goldenOrganisationUnit.setUser("NjbJCa6JkQu");
        goldenOrganisationUnit.setShortName("KE - HNQIS SF pilot test facility 1");
        goldenOrganisationUnit.setDisplayName("KE - HNQIS SF pilot test facility 1");
        goldenOrganisationUnit.setDisplayShortName("KE - HNQIS SF pilot test facility 1");
        goldenOrganisationUnit.setExternalAccess(false);
        goldenOrganisationUnit.setPath("/FvUGp8I75zV/FhYFWRnrbkd/rP1W74RpNWF/AVqhgx4Ov2F/yp9x1IMVvcL/uJeWnsfj5EN/spT8zFVQsvx/QS7sK8XzdQc");
        goldenOrganisationUnit.setFeatureType("NONE");
        goldenOrganisationUnit.setOpeningDate("2015-08-06");//2015-08-06T00:00:00.000+0000
        goldenOrganisationUnit.setDimensionItem("QS7sK8XzdQc");

        goldenOrganisationUnitGroups.add("xdnfH7jiCUp");
        goldenOrganisationUnitGroups.add("NAHlpJzIfbi");

        goldenDataSets.add("oMlpyyPeJI1");
        goldenDataSets.add("oaFG0Z4EFHo");
        goldenDataSets.add("lI4BBizJsx0");
    }

    public void createRealSdkProgram(){
        //sdk program
        goldenSdkProgram =new org.hisp.dhis.android.sdk.persistence.models.Program();
        goldenSdkProgram.setName("KE HNQIS Family Planning");
        goldenSdkProgram.setDisplayName("KE HNQIS Family Planning");
        goldenSdkProgram.setCreated("2015-10-16T13:51:32.264+0000");
        goldenSdkProgram.setLastUpdated("2016-02-03T19:58:35.161+0000");
        //goldenSdkProgram.setAccess();//{"delete":false,"externalize":false,"manage":true,"read":true,"update":true,"write":true}
        goldenSdkProgram.setTrackedEntity(null);
        //goldenSdkProgram.setProgramType(new ProgramType("without_registration"));
        goldenSdkProgram.setVersion(3);
        goldenSdkProgram.setEnrollmentDateLabel(null);
        goldenSdkProgram.setDescription(null);
        goldenSdkProgram.setOnlyEnrollOnce(false);
        goldenSdkProgram.setExtenalAccess(false);
        goldenSdkProgram.setDisplayIncidentDate(false);
        goldenSdkProgram.setIncidentDateLabel(null);
        goldenSdkProgram.setRegistration(false);
        goldenSdkProgram.setSelectEnrollmentDatesInFuture(false);
        goldenSdkProgram.setDataEntryMethod(false);
        goldenSdkProgram.setSingleEvent(false);
        goldenSdkProgram.setIgnoreOverdueEvents(false);
        goldenSdkProgram.setRelationshipFromA(false);
        goldenSdkProgram.setSelectIncidentDatesInFuture(false);
    }

    public void createRealAppProgram(){
        goldenProgram = new Program("wK0958s1bdj","KE HNQIS Family Planning");
    }

    public void createRealOrgUnit(){
        //Create the Test Program and OrgUnit to compare with real data.
        //Fixme the orgunitlevel is not pulled.
        //goldenOrgUnit.setOrgUnitLevel(new OrgUnitLevel("Zone"));
        goldenOrgUnit= new OrgUnit("QS7sK8XzdQc","KE - HNQIS SF pilot test facility 1",null,null);
        goldenOrgUnit.setProductivity(10);
        goldenOrgUnit.addProgram(goldenProgram);
    }

    private void testProgramAttribute(org.hisp.dhis.android.sdk.persistence.models.Program sdkProgram) {
        List<ProgramAttributeValue> attributeValues=sdkProgram.getAttributeValues();
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

    private void testOrgUnitPrograms(org.hisp.dhis.android.sdk.persistence.models.Program sdkProgram) {
        //Test Program in app DB has been saved with the correct propierties
        assertTrue(goldenProgram.getUid().equals(sdkProgram.getUid()));
        assertTrue(goldenProgram.getName().equals(sdkProgram.getName()));
    }

    private void testOrgUnit(OrgUnit appOrgUnit) {
        assertTrue(goldenOrgUnit.getName().equals(appOrgUnit.getName()));
        assertTrue(goldenOrgUnit.getUid().equals(appOrgUnit.getUid()));
        assertTrue(goldenOrgUnit.getProductivity().equals(appOrgUnit.getProductivity()));
        //Fixme the orgunitlevel is not pulled.
        //assertTrue(goldenOrgUnit.getOrgUnitLevel().getName().equals(appOrgUnit.getOrgUnitLevel().getName()));
        assertTrue(goldenProgram.getUid().equals(appOrgUnit.getPrograms().get(0).getUid()));
    }

    private void testSdkProgram(org.hisp.dhis.android.sdk.persistence.models.Program sdkProgram) {

        assertTrue(goldenSdkProgram.getName().equals(sdkProgram.getName()));
        assertTrue(goldenSdkProgram.getDisplayName().equals(sdkProgram.getDisplayName()));
        assertTrue(goldenSdkProgram.getCreated().equals(sdkProgram.getCreated()));
        assertTrue(goldenSdkProgram.getLastUpdated().equals(sdkProgram.getLastUpdated()));
        assertTrue(goldenSdkProgram.getVersion() == (sdkProgram.getVersion()));
        assertTrue(goldenSdkProgram.getOnlyEnrollOnce()==(sdkProgram.getOnlyEnrollOnce()));
        assertTrue(goldenSdkProgram.getExtenalAccess()==(sdkProgram.getExtenalAccess()));
        assertTrue(goldenSdkProgram.getDisplayIncidentDate()==(sdkProgram.getDisplayIncidentDate()));
        assertTrue(goldenSdkProgram.getRegistration()==(sdkProgram.getRegistration()));
        assertTrue(goldenSdkProgram.getSelectEnrollmentDatesInFuture()==(sdkProgram.getSelectEnrollmentDatesInFuture()));
        assertTrue(goldenSdkProgram.getDataEntryMethod()==(sdkProgram.getDataEntryMethod()));
        assertTrue(goldenSdkProgram.getSingleEvent()==(sdkProgram.getSingleEvent()));
        assertTrue(goldenSdkProgram.getIgnoreOverdueEvents()==(sdkProgram.getIgnoreOverdueEvents()));
        assertTrue(goldenSdkProgram.getRelationshipFromA()==(sdkProgram.getRelationshipFromA()));
        assertTrue(goldenSdkProgram.getSelectIncidentDatesInFuture()==(sdkProgram.getSelectIncidentDatesInFuture()));

        //this values are null.
        assertTrue(goldenSdkProgram.getTrackedEntity() == (sdkProgram.getTrackedEntity()));
        assertTrue(goldenSdkProgram.getDescription()==(sdkProgram.getDescription()));
        assertTrue(goldenSdkProgram.getEnrollmentDateLabel()==(sdkProgram.getEnrollmentDateLabel()));
        assertTrue(goldenSdkProgram.getIncidentDateLabel()==(sdkProgram.getIncidentDateLabel()));

        assertTrue(goldenSdkProgram.getIncidentDateLabel() == (sdkProgram.getIncidentDateLabel()));
        Access access= sdkProgram.getAccess();

        //Real data->{"delete":false,"externalize":false,"manage":true,"read":true,"update":true,"write":true}
        assertTrue(access.isWrite());
        assertTrue(access.isUpdate());
        assertTrue(access.isRead());
        assertTrue(access.isManage());
        assertTrue(!access.isExternalize());
        assertTrue(!access.isDelete());

        ProgramType programType=sdkProgram.getProgramType();
        assertTrue(programType.getValue().equals(PROGRAM_PROGRAMTYPE));
    }

    private void testOrganisationUnitDataSets(OrganisationUnit sdkOrganisationUnit) {
        for(String organisationUnitDataSetUid: goldenDataSets) {
            boolean isInSdk=false;
            for (OrganisationUnitDataSet organisationUnitDataSetsSdk : SDKTestUtils.getOrganisationUnitDataSets(sdkOrganisationUnit.getId())) {
                if(organisationUnitDataSetsSdk.getDataSetId().equals(organisationUnitDataSetUid))
                    isInSdk=true;
            }
            assertTrue(isInSdk);
        }
    }

    private void testOrganisationUnitGroups(OrganisationUnit sdkOrganisationUnit) {
        for(String organisationUnitGroupsUid: goldenOrganisationUnitGroups) {
            boolean isInSdk=false;
            for (OrganisationUnitGroup organisationUnitGroupsSdk : SDKTestUtils.getOrganisationUnitGroups(sdkOrganisationUnit.getId())) {
                if(organisationUnitGroupsSdk.getOrganisationUnitGroupId().equals(organisationUnitGroupsUid))
                    isInSdk=true;
            }
            assertTrue(isInSdk);
        }
    }

    private void testSdkOrganisationUnit(OrganisationUnit sdkOrganisationUnit) {
        assertTrue(sdkOrganisationUnit.getLabel().equals(goldenOrganisationUnit.getLabel()));
        assertTrue(sdkOrganisationUnit.getUuid().equals(goldenOrganisationUnit.getUuid()));
        assertTrue(sdkOrganisationUnit.getLevel()== goldenOrganisationUnit.getLevel());
        assertTrue(sdkOrganisationUnit.getParent().equals(goldenOrganisationUnit.getParent()));
        assertTrue(sdkOrganisationUnit.getUuid().equals(goldenOrganisationUnit.getUuid()));
        assertTrue(sdkOrganisationUnit.getLastUpdated().equals(goldenOrganisationUnit.getLastUpdated()));
        assertTrue(sdkOrganisationUnit.getCreated().equals(goldenOrganisationUnit.getCreated()));
        assertTrue(sdkOrganisationUnit.getName().equals(goldenOrganisationUnit.getName()));
        assertTrue(sdkOrganisationUnit.getShortName().equals(goldenOrganisationUnit.getShortName()));
        assertTrue(sdkOrganisationUnit.getDisplayName().equals(goldenOrganisationUnit.getDisplayName()));
        assertTrue(sdkOrganisationUnit.getDisplayShortName().equals(goldenOrganisationUnit.getDisplayShortName()));
        assertTrue(sdkOrganisationUnit.getExternalAccess().equals(goldenOrganisationUnit.getExternalAccess()));
        assertTrue(sdkOrganisationUnit.getPath().equals(goldenOrganisationUnit.getPath()));
        assertTrue(sdkOrganisationUnit.getFeatureType().equals(goldenOrganisationUnit.getFeatureType()));
        assertTrue(sdkOrganisationUnit.getOpeningDate().equals(goldenOrganisationUnit.getOpeningDate()));
        assertTrue(sdkOrganisationUnit.getDimensionItem().equals(goldenOrganisationUnit.getDimensionItem()));

        testOrganisationUnitGroups(sdkOrganisationUnit);

        testOrganisationUnitDataSets(sdkOrganisationUnit);
    }

}