package org.eyeseetea.malariacare.test.pull;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.fitness.data.DataSet;

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
    private final String PROGRAM_PROGRAMTYPE="without_registration";

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

        //Create program and organisationUnit to test the download and saved objects in SDK DB.
        OrganisationUnit organisationUnitGolden=new OrganisationUnit();
        organisationUnitGolden.setId("QS7sK8XzdQc");
        organisationUnitGolden.setLabel("KE - HNQIS SF pilot test facility 1");
        organisationUnitGolden.setLevel(8);
        organisationUnitGolden.setParent("spT8zFVQsvx");
        organisationUnitGolden.setUuid("68c66a34-806f-49d1-9593-ba5d399ce95e");
        organisationUnitGolden.setLastUpdated("2016-01-29T17:47:13.909+0000");
        organisationUnitGolden.setCreated("2015-08-06T12:25:04.675+0000");
        organisationUnitGolden.setName("KE - HNQIS SF pilot test facility 1");
        organisationUnitGolden.setUser("NjbJCa6JkQu");
        organisationUnitGolden.setShortName("KE - HNQIS SF pilot test facility 1");
        organisationUnitGolden.setDisplayName("KE - HNQIS SF pilot test facility 1");
        organisationUnitGolden.setDisplayShortName("KE - HNQIS SF pilot test facility 1");
        organisationUnitGolden.setExternalAccess(false);
        organisationUnitGolden.setPath("/FvUGp8I75zV/FhYFWRnrbkd/rP1W74RpNWF/AVqhgx4Ov2F/yp9x1IMVvcL/uJeWnsfj5EN/spT8zFVQsvx/QS7sK8XzdQc");
        organisationUnitGolden.setFeatureType("NONE");
        organisationUnitGolden.setOpeningDate("2015-08-06");//2015-08-06T00:00:00.000+0000
        organisationUnitGolden.setDimensionItem("QS7sK8XzdQc");
        List<String> dataSets=new ArrayList<>();
        List<String> organisationUnitGroups=new ArrayList<>();

        organisationUnitGroups.add("xdnfH7jiCUp");
        organisationUnitGroups.add("NAHlpJzIfbi");

        dataSets.add("oMlpyyPeJI1");
        dataSets.add("oaFG0Z4EFHo");
        dataSets.add("lI4BBizJsx0");

        //sdk program
        org.hisp.dhis.android.sdk.persistence.models.Program programGolden=new org.hisp.dhis.android.sdk.persistence.models.Program();
        programGolden.setName("KE HNQIS Family Planning");
        programGolden.setDisplayName("KE HNQIS Family Planning");
        programGolden.setCreated("2015-10-16T13:51:32.264+0000");
        programGolden.setLastUpdated("2016-02-03T19:58:35.161+0000");
        //programGolden.setAccess();//{"delete":false,"externalize":false,"manage":true,"read":true,"update":true,"write":true}
        programGolden.setTrackedEntity(null);
        //programGolden.setProgramType(new ProgramType("without_registration"));
        programGolden.setVersion(3);
        programGolden.setEnrollmentDateLabel(null);
        programGolden.setDescription(null);
        programGolden.setOnlyEnrollOnce(false);
        programGolden.setExtenalAccess(false);
        programGolden.setDisplayIncidentDate(false);
        programGolden.setIncidentDateLabel(null);
        programGolden.setRegistration(false);
        programGolden.setSelectEnrollmentDatesInFuture(false);
        programGolden.setDataEntryMethod(false);
        programGolden.setSingleEvent(false);
        programGolden.setIgnoreOverdueEvents(false);
        programGolden.setRelationshipFromA(false);
        programGolden.setSelectIncidentDatesInFuture(false);

        //Create the Test Program and OrgUnit to compare with real data.
        Program testProgram= new Program("wK0958s1bdj","KE HNQIS Family Planning");
        OrgUnit testOrgUnit= new OrgUnit("QS7sK8XzdQc","KE - HNQIS SF pilot test facility 1",null,null);
        testOrgUnit.setProductivity(10);
        testOrgUnit.addProgram(testProgram);
        //Fixme the orgunitlevel is not pulled.
        //testOrgUnit.setOrgUnitLevel(new OrgUnitLevel("Zone"));


        //Test organisationUnit has been downloaded with the correct propierties.
        OrganisationUnit sdkOrganisationUnit=SDKTestUtils.getOrganisationUnit(organisationUnitGolden.getId());
        assertTrue(sdkOrganisationUnit.getLabel().equals(organisationUnitGolden.getLabel()));
        assertTrue(sdkOrganisationUnit.getUuid().equals(organisationUnitGolden.getUuid()));
        assertTrue(sdkOrganisationUnit.getLevel()==organisationUnitGolden.getLevel());
        assertTrue(sdkOrganisationUnit.getParent().equals(organisationUnitGolden.getParent()));
        assertTrue(sdkOrganisationUnit.getUuid().equals(organisationUnitGolden.getUuid()));
        assertTrue(sdkOrganisationUnit.getLastUpdated().equals(organisationUnitGolden.getLastUpdated()));
        assertTrue(sdkOrganisationUnit.getCreated().equals(organisationUnitGolden.getCreated()));
        assertTrue(sdkOrganisationUnit.getName().equals(organisationUnitGolden.getName()));
        assertTrue(sdkOrganisationUnit.getShortName().equals(organisationUnitGolden.getShortName()));
        assertTrue(sdkOrganisationUnit.getDisplayName().equals(organisationUnitGolden.getDisplayName()));
        assertTrue(sdkOrganisationUnit.getDisplayShortName().equals(organisationUnitGolden.getDisplayShortName()));
        assertTrue(sdkOrganisationUnit.getExternalAccess().equals(organisationUnitGolden.getExternalAccess()));
        assertTrue(sdkOrganisationUnit.getPath().equals(organisationUnitGolden.getPath()));
        assertTrue(sdkOrganisationUnit.getFeatureType().equals(organisationUnitGolden.getFeatureType()));
        assertTrue(sdkOrganisationUnit.getOpeningDate().equals(organisationUnitGolden.getOpeningDate()));
        assertTrue(sdkOrganisationUnit.getDimensionItem().equals(organisationUnitGolden.getDimensionItem()));

        for(String organisationUnitGroupsUid:organisationUnitGroups) {
            boolean isInSdk=false;
            for (OrganisationUnitGroup organisationUnitGroupsSdk : SDKTestUtils.getOrganisationUnitGroups(sdkOrganisationUnit.getId())) {
                if(organisationUnitGroupsSdk.getOrganisationUnitGroupId().equals(organisationUnitGroupsUid))
                    isInSdk=true;
            }
            assertTrue(isInSdk);
        }
        for(String organisationUnitDataSetUid:dataSets) {
            boolean isInSdk=false;
            for (OrganisationUnitDataSet organisationUnitDataSetsSdk : SDKTestUtils.getOrganisationUnitDataSets(sdkOrganisationUnit.getId())) {
                if(organisationUnitDataSetsSdk.getDataSetId().equals(organisationUnitDataSetUid))
                    isInSdk=true;
            }
            assertTrue(isInSdk);
        }

        //Test program (in sdk) has been downloaded with the correct propierties.
        org.hisp.dhis.android.sdk.persistence.models.Program sdkProgram=SDKTestUtils.getSDKProgram(testProgram.getUid());
        assertTrue(programGolden.getName().equals(sdkProgram.getName()));
        assertTrue(programGolden.getDisplayName().equals(sdkProgram.getDisplayName()));
        assertTrue(programGolden.getCreated().equals(sdkProgram.getCreated()));
        assertTrue(programGolden.getLastUpdated().equals(sdkProgram.getLastUpdated()));
        assertTrue(programGolden.getVersion() == (sdkProgram.getVersion()));
        assertTrue(programGolden.getOnlyEnrollOnce()==(sdkProgram.getOnlyEnrollOnce()));
        assertTrue(programGolden.getExtenalAccess()==(sdkProgram.getExtenalAccess()));
        assertTrue(programGolden.getDisplayIncidentDate()==(sdkProgram.getDisplayIncidentDate()));
        assertTrue(programGolden.getRegistration()==(sdkProgram.getRegistration()));
        assertTrue(programGolden.getSelectEnrollmentDatesInFuture()==(sdkProgram.getSelectEnrollmentDatesInFuture()));
        assertTrue(programGolden.getDataEntryMethod()==(sdkProgram.getDataEntryMethod()));
        assertTrue(programGolden.getSingleEvent()==(sdkProgram.getSingleEvent()));
        assertTrue(programGolden.getIgnoreOverdueEvents()==(sdkProgram.getIgnoreOverdueEvents()));
        assertTrue(programGolden.getRelationshipFromA()==(sdkProgram.getRelationshipFromA()));
        assertTrue(programGolden.getSelectIncidentDatesInFuture()==(sdkProgram.getSelectIncidentDatesInFuture()));


        //this values are null.
        assertTrue(programGolden.getTrackedEntity() == (sdkProgram.getTrackedEntity()));
        assertTrue(programGolden.getDescription()==(sdkProgram.getDescription()));
        assertTrue(programGolden.getEnrollmentDateLabel()==(sdkProgram.getEnrollmentDateLabel()));
        assertTrue(programGolden.getIncidentDateLabel()==(sdkProgram.getIncidentDateLabel()));

        assertTrue(programGolden.getIncidentDateLabel() == (sdkProgram.getIncidentDateLabel()));
        Access access= sdkProgram.getAccess();
        //{"delete":false,"externalize":false,"manage":true,"read":true,"update":true,"write":true}
        assertTrue(access.isWrite());
        assertTrue(access.isUpdate());
        assertTrue(access.isRead());
        assertTrue(access.isManage());
        assertTrue(!access.isExternalize());
        assertTrue(!access.isDelete());

        ProgramType programType=sdkProgram.getProgramType();
        assertTrue(programType.getValue().equals(PROGRAM_PROGRAMTYPE));

        //Test orgUnit in app DB has been saved with the correct propierties.

        OrgUnit appOrgUnit=SDKTestUtils.getOrgUnit(testOrgUnit.getUid());
        assertTrue(testOrgUnit.getName().equals(appOrgUnit.getName()));
        assertTrue(testOrgUnit.getUid().equals(appOrgUnit.getUid()));
        assertTrue(testOrgUnit.getProductivity().equals(appOrgUnit.getProductivity()));
        //Fixme the orgunitlevel is not pulled.
        //assertTrue(testOrgUnit.getOrgUnitLevel().getName().equals(appOrgUnit.getOrgUnitLevel().getName()));
        assertTrue(testProgram.getUid().equals(appOrgUnit.getPrograms().get(0).getUid()));

        //Test Program in app DB has been saved with the correct propierties
        assertTrue(testProgram.getUid().equals(sdkProgram.getUid()));
        assertTrue(testProgram.getName().equals(sdkProgram.getName()));
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

}