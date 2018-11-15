package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.domain.usecase.LocalSurveyFilter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

public class SurveyFilterShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createFilterGetQuarantine(){
        String programUId = "programUId";
        String orgUnitUid = "orgUnitUid";

        LocalSurveyFilter surveyFilter = LocalSurveyFilter.createGetQuarantineSurveys(programUId, orgUnitUid);

        Assert.assertNotNull(surveyFilter);
        Assert.assertTrue(surveyFilter.getProgramUId().equals(programUId));
        Assert.assertTrue(surveyFilter.getOrgUnitUId().equals(orgUnitUid));
        Assert.assertTrue(surveyFilter.isQuarantineSurvey());
        Assert.assertNull(surveyFilter.getStartDate());
        Assert.assertNull(surveyFilter.getEndDate());
    }

    @Test
    public void createCheckQuarantineOnServerFilter(){
        Date startDate = new Date();
        Date endDate = new Date();
        String programUId = "programUId";
        String orgUnitUid = "orgUnitUid";
        LocalSurveyFilter surveyFilter = LocalSurveyFilter.createCheckQuarantineOnServerFilter(startDate, endDate, programUId, orgUnitUid);

        Assert.assertNotNull(surveyFilter);
        Assert.assertTrue(surveyFilter.getProgramUId().equals(programUId));
        Assert.assertTrue(surveyFilter.getOrgUnitUId().equals(orgUnitUid));
        Assert.assertTrue(surveyFilter.getStartDate().equals(startDate));
        Assert.assertTrue(surveyFilter.getEndDate().equals(endDate));
        Assert.assertTrue(surveyFilter.isQuarantineSurvey());
    }

    @Test
    public void throw_exception_when_create_enddate_after_startdate(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("End date should be before than start Date");
        Date endDate = new Date();
        Date startDate = new Date();
        startDate.setTime(startDate.getTime()+1000);
        String programUId = "programUId";
        String orgUnitUid = "orgUnitUid";
        LocalSurveyFilter.createCheckQuarantineOnServerFilter(startDate, endDate, programUId, orgUnitUid);
    }

    @Test
    public void throw_exception_when_create_quarantine_from_server_without_program(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("programUId is required");
        LocalSurveyFilter.createCheckQuarantineOnServerFilter(new Date(), new Date(), null,"orgUnitUid");
    }

    @Test
    public void throw_exception_when_create_quarantine_from_server_without_orgUnit(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("orgUnitUId is required");
        LocalSurveyFilter.createCheckQuarantineOnServerFilter(new Date(), new Date(), "programUId", null);
    }

    @Test
    public void throw_exception_when_create_quarantine_from_server_without_startDate(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("startDate is required");
        LocalSurveyFilter.createCheckQuarantineOnServerFilter(null, new Date(), "programUId", "orgUnitUid");
    }

    @Test
    public void throw_exception_when_create_quarantine_from_server_without_enddate(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("endDate is required");
        LocalSurveyFilter.createCheckQuarantineOnServerFilter(new Date(), null, "programUId", "orgUnitUid");
    }

    @Test
    public void throw_exception_when_create_quarantine_from_local_without_program(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("programUId is required");
        LocalSurveyFilter.createGetQuarantineSurveys(null,"orgUnitUid");
    }

    @Test
    public void throw_exception_when_create_quarantine_from_local_without_orgunit(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("orgUnitUId is required");
        LocalSurveyFilter.createGetQuarantineSurveys("programUId", null);
    }
}
