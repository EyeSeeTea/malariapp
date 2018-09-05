package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

public class SurveyFilterShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createFilterForPull(){
        Date date = new Date();
        int events = 10;

        SurveyFilter surveyFilter = SurveyFilter.createGetSurveysOnPull(date, events);

        Assert.assertNotNull(surveyFilter);
        Assert.assertTrue(surveyFilter.getMaxEvents()==events);
        Assert.assertTrue(surveyFilter.getStartDate().equals(date));
        Assert.assertFalse(surveyFilter.isQuarantineSurvey());
        Assert.assertNull(surveyFilter.getEndDate());
        Assert.assertNull(surveyFilter.getOrgUnitUId());
        Assert.assertNull(surveyFilter.getProgramUId());
    }

    @Test
    public void createFilterGetQuarantine(){
        String programUId = "programUId";
        String orgUnitUid = "orgUnitUid";

        SurveyFilter surveyFilter = SurveyFilter.createGetQuarantineSurveys(programUId, orgUnitUid);

        Assert.assertNotNull(surveyFilter);
        Assert.assertTrue(surveyFilter.getProgramUId().equals(programUId));
        Assert.assertTrue(surveyFilter.getOrgUnitUId().equals(orgUnitUid));
        Assert.assertTrue(surveyFilter.isQuarantineSurvey());
        Assert.assertNull(surveyFilter.getMaxEvents());
        Assert.assertNull(surveyFilter.getStartDate());
        Assert.assertNull(surveyFilter.getEndDate());
    }

    @Test
    public void createCheckQuarantineOnServerFilter(){
        Date startDate = new Date();
        Date endDate = new Date();
        String programUId = "programUId";
        String orgUnitUid = "orgUnitUid";
        SurveyFilter surveyFilter = SurveyFilter.createCheckQuarantineOnServerFilter(startDate, endDate, programUId, orgUnitUid);

        Assert.assertNotNull(surveyFilter);
        Assert.assertTrue(surveyFilter.getProgramUId().equals(programUId));
        Assert.assertTrue(surveyFilter.getOrgUnitUId().equals(orgUnitUid));
        Assert.assertTrue(surveyFilter.getStartDate().equals(startDate));
        Assert.assertTrue(surveyFilter.getEndDate().equals(endDate));
        Assert.assertTrue(surveyFilter.isQuarantineSurvey());
        Assert.assertNull(surveyFilter.getMaxEvents());
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
        SurveyFilter.createCheckQuarantineOnServerFilter(startDate, endDate, programUId, orgUnitUid);
    }

    @Test
    public void throw_exception_when_create_quarantine_from_server_without_program(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("programUId is required");
        SurveyFilter.createCheckQuarantineOnServerFilter(new Date(), new Date(), null,"orgUnitUid");
    }

    @Test
    public void throw_exception_when_create_quarantine_from_server_without_orgUnit(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("orgUnitUId is required");
        SurveyFilter.createCheckQuarantineOnServerFilter(new Date(), new Date(), "programUId", null);
    }

    @Test
    public void throw_exception_when_create_quarantine_from_server_without_startDate(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("startDate is required");
        SurveyFilter.createCheckQuarantineOnServerFilter(null, new Date(), "programUId", "orgUnitUid");
    }

    @Test
    public void throw_exception_when_create_quarantine_from_server_without_enddate(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("endDate is required");
        SurveyFilter.createCheckQuarantineOnServerFilter(new Date(), null, "programUId", "orgUnitUid");
    }

    @Test
    public void throw_exception_when_create_quarantine_from_local_without_program(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("programUId is required");
        SurveyFilter.createGetQuarantineSurveys(null,"orgUnitUid");
    }

    @Test
    public void throw_exception_when_create_quarantine_from_local_without_orgunit(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("orgUnitUId is required");
        SurveyFilter.createGetQuarantineSurveys("programUId", null);
    }
}
