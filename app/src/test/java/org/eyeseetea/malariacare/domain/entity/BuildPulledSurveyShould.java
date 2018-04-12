package org.eyeseetea.malariacare.domain.entity;


import static org.eyeseetea.malariacare.domain.entity.Survey.COMPLETION_DATE_REQUIRED;
import static org.eyeseetea.malariacare.domain.entity.Survey.CREATION_DATE_REQUIRED;
import static org.eyeseetea.malariacare.domain.entity.Survey.EVENT_UID_REQUIRED;
import static org.eyeseetea.malariacare.domain.entity.Survey.ORG_UNIT_REQUIRED;
import static org.eyeseetea.malariacare.domain.entity.Survey.PROGRAM_REQUIRED;
import static org.eyeseetea.malariacare.domain.entity.Survey.SCHEDULED_DATE_REQUIRED;
import static org.eyeseetea.malariacare.domain.entity.Survey.UPLOAD_DATE_REQUIRED;
import static org.eyeseetea.malariacare.domain.entity.Survey.VALUE_UIDS_REQUIRED;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Date;

public class BuildPulledSurveyShould {

    Date creationDate = new Date();
    Date uploadDate = new Date();
    Date completionDate = new Date();
    Date scheludedDate = new Date();
    ArrayList<Integer> values = new ArrayList<Integer>();
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void return_mandatory_values_for_each_getter_after_new_survey_is_created() throws Exception {
        Survey survey = Survey.buildPulledSurvey("programUId",
                "orgUnitUId", creationDate,
                uploadDate, completionDate, scheludedDate, "eventUid", values).build();

        Assert.assertTrue(survey.programUId().equals("programUId"));
        Assert.assertTrue(survey.orgUnitUId().equals("orgUnitUId"));
        Assert.assertTrue(survey.status().equals(Survey.Status.SENT));
        Assert.assertTrue(survey.creationDate().equals(creationDate));
        Assert.assertTrue(survey.uploadDate().equals(uploadDate));
        Assert.assertTrue(survey.completionDate().equals(completionDate));
        Assert.assertTrue(survey.scheduledDate().equals(scheludedDate));
        Assert.assertTrue(survey.referencedEventUId().equals("eventUid"));
        Assert.assertTrue(survey.valueIds().equals(values));
    }

    @Test
    public void throw_exception_if_pulled_survey_program_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(PROGRAM_REQUIRED);

        Survey survey = Survey.buildPulledSurvey(null,
                "orgUnitUId", creationDate,
                uploadDate, completionDate, scheludedDate, "eventUid", values).build();
    }

    @Test
    public void throw_exception_if_pulled_survey_orgunit_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ORG_UNIT_REQUIRED);
        
        Survey survey = Survey.buildPulledSurvey("programUId",
                null, creationDate,
                uploadDate, completionDate, scheludedDate, "eventUid", values).build();
    }

    @Test
    public void throw_exception_if_pulled_survey_creation_date_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(CREATION_DATE_REQUIRED);

        Survey survey = Survey.buildPulledSurvey("programUId",
                "orgUnitUId", null,
                completionDate, uploadDate, scheludedDate, "eventUid", values).build();
    }

    @Test
    public void throw_exception_if_pulled_survey_upload_date_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(UPLOAD_DATE_REQUIRED);

        Survey survey = Survey.buildPulledSurvey("programUId",
                "orgUnitUId", creationDate,
                completionDate, null, scheludedDate, "eventUid", values).build();
    }
    @Test
    public void throw_exception_if_pulled_survey_completion_date_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(COMPLETION_DATE_REQUIRED);

        Survey survey = Survey.buildPulledSurvey("programUId",
                "orgUnitUId", creationDate,
                null, uploadDate, scheludedDate, "eventUid", values).build();
    }
    @Test
    public void throw_exception_if_pulled_survey_scheduled_date_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(SCHEDULED_DATE_REQUIRED);

        Survey survey = Survey.buildPulledSurvey("programUId",
                "orgUnitUId", creationDate,
                completionDate, uploadDate, null, "eventUid", values).build();
    }
    @Test
    public void throw_exception_if_pulled_survey_event_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(EVENT_UID_REQUIRED);

        Survey survey = Survey.buildPulledSurvey("programUId",
                "orgUnitUId", creationDate,
                completionDate, uploadDate, scheludedDate, null, values).build();
    }
    @Test
    public void throw_exception_if_pulled_survey_value_uids_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(VALUE_UIDS_REQUIRED);

        Survey survey = Survey.buildPulledSurvey("programUId",
                "orgUnitUId", creationDate,
                completionDate, uploadDate, scheludedDate, "eventUId", null).build();
    }
}