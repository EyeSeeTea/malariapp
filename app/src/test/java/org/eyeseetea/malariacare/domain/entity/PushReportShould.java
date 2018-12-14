package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eyeseetea.malariacare.data.sync.mappers.PushReportMapper;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushConflict;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushedValuesCount;
import org.hisp.dhis.client.sdk.core.common.network.ApiMessage;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PushReportShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public final String DATAVALUES_IMPORTED_SUMMARY_JSON =
            "{\"httpStatus\":\"OK\", \"httpStatusCode\":\"200\", \"message\":\"Import was successful.\", "
                    + "\"status\":\"OK\", "
                    + "\"response\":{\"responseType\":\"ImportSummaries\", \"status\":\"SUCCESS\", "
                    + "\"importSummaries\":[{\"responseType\":\"ImportSummary\",\"status\":\"SUCCESS\","
                    + "\"description\": \"\",  \"importCount\""
                    + ":{ \"imported\": \"0 \",  \"updated\": \"4 \",  \"ignored\": \"4 \",  \"deleted\": \"4 \"}, "
                    + " \"reference\": \"DSifqmkzKfJ\", "
                    + "\"href\": \"https://old-staging.psi-mis.org/api/events/DSifqmkzKfJ\"}]}}";

    private String API_MESSAGE_WITH_CONFLICTS_JSON ="{\"httpStatus\":\"Conflict\",\"httpStatusCode\":409,"
            + "\"status\":\"WARNING\",\"message\":\"One more conflicts encountered, please check "
            + "import summary.\","
            + "\"response\":{\"responseType\":\"ImportSummaries\","
            + "\"status\":\"SUCCESS\",\"imported\":3,\"updated\":0,\"deleted\":0,\"ignored\":1,"
            + "\"importSummaries\":[{\"responseType\":\"ImportSummary\",\"status\":\"SUCCESS\","
            + "\"importCount\":{\"imported\":3,\"updated\":0,\"ignored\":1,\"deleted\":0},"
            + "\"conflicts\":[{\"object\":\"qWMb2UM2ikL\","
            + "\"value\":\"value_not_valid_datetime\"}],\"reference\":\"wqee94y5wzT\","
            + "\"href\":\"https://old-staging.psi-mis.org/api/events/wqee94y5wzT\"}]}}";

    public final String ERROR_IMPORT_SUMMARY_JSON =
            "{\"httpStatus\":\"OK\", \"httpStatusCode\":\"400\", \"message\":\"Import was successful.\", "
                    + "\"status\":\"OK\", "
                    + "\"response\":{\"responseType\":\"ImportSummaries\", \"status\":\"ERROR\", "
                    + "\"importSummaries\":[{\"responseType\":\"ImportSummary\",\"status\":\"ERROR\","
                    + "\"description\": \"\",  \"importCount\""
                    + ":{ \"imported\": \"4 \",  \"updated\": \"0 \",  \"ignored\": \"0 \",  \"deleted\": \"0 \"}, "
                    + " \"reference\": \"DSifqmkzKfJ\", "
                    + "\"href\": \"https://old-staging.psi-mis.org/api/events/DSifqmkzKfJ\"}]}}";

    public final String API_MESSAGE_WITH_CONFLICTS_JSON_KEY = "wqee94y5wzT";

    @Test
    public void import_summary_valid_push() {
        ImportSummary importSummary = getImportSummary(API_MESSAGE_WITH_CONFLICTS_JSON);
        PushReport pushReport = PushReportMapper.mapFromImportSummaryToPushReport(importSummary,
                API_MESSAGE_WITH_CONFLICTS_JSON_KEY);
        assertThat(!pushReport.hasPushErrors(), is(true));
    }

    @Test
    public void error_push() {
        ImportSummary importSummary = getImportSummary(ERROR_IMPORT_SUMMARY_JSON);
        PushReport pushReport = PushReportMapper.mapFromImportSummaryToPushReport(importSummary,
                API_MESSAGE_WITH_CONFLICTS_JSON_KEY);
        assertThat(!pushReport.hasPushErrors(), is(false));
    }

    @Test
    public void error_not_datavalues_imported_push() {
        ImportSummary importSummary = getImportSummary(DATAVALUES_IMPORTED_SUMMARY_JSON);
        PushReport pushReport = PushReportMapper.mapFromImportSummaryToPushReport(importSummary,
                API_MESSAGE_WITH_CONFLICTS_JSON_KEY);
        assertThat(!pushReport.hasPushErrors(), is(false));
    }


    @Test
    public void throw_exception_when_create_a_push_report_with_null_uid() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("EventUid is required");
        new PushReport(null, PushReport.Status.OK, null, null, null, null, null);
    }

    @Test
    public void throw_exception_when_create_a_push_report_with_empty_uid() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("EventUid is required");
        new PushReport("", PushReport.Status.OK, null, null, null, null, null);
    }

    @Test
    public void throw_exception_when_create_a_push_report_with_null_status() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Status is required");
        new PushReport("doijh87", null, null, null, null, null, null);
    }

    @Test
    public void get_event_uid_return_correct_uid() {
        String uid = "doijh87";
        PushReport pushReport = new PushReport(uid, PushReport.Status.OK, null, null, null, null, null);
        assertThat(pushReport.getEventUid(), is(uid));
    }

    @Test
    public void get_status_return_correct_status() {
        PushReport.Status status = PushReport.Status.OK;
        PushReport pushReport = new PushReport("doijh87", status, null, null, null, null, null);
        assertThat(pushReport.getStatus(), is(status));
    }

    @Test
    public void get_descrition_return_correct_description() {
        String description = "test description";
        PushReport pushReport = new PushReport("doijh87", PushReport.Status.OK, description, null,
                null, null, null);
        assertThat(pushReport.getDescription(), is(description));
    }

    @Test
    public void get_pushValuesCount_return_correct_pushValuesCount() {
        PushedValuesCount   pushedValuesCount = new PushedValuesCount(0,0,0,0);
        PushReport pushReport = new PushReport("doijh87", PushReport.Status.OK, "", pushedValuesCount,
                null, null, null);
        assertThat(pushReport.getPushedValues(), is(pushedValuesCount));
    }

    @Test
    public void get_reference_return_correct_reference() {
        String reference="DSifqmkzKfJ";
        PushReport pushReport = new PushReport("doijh87", PushReport.Status.OK, null, null,
                reference, null, null);
        assertThat(pushReport.getReference(), is(reference));
    }

    @Test
    public void get_href_return_correct_href() {
        String href="https://old-staging.psi-mis.org/api/events/DSifqmkzKfJ";
        PushReport pushReport = new PushReport("doijh87", PushReport.Status.OK, null, null,
                null, href, null);
        assertThat(pushReport.getHref(), is(href));
    }

    @Test
    public void get_pushConflicts_return_correct_pushConflicts() {
        List<PushConflict> pushConflicts=new ArrayList<>();
        PushReport pushReport = new PushReport("doijh87", PushReport.Status.OK, null, null,
                null, null, pushConflicts);
        assertThat(pushReport.getPushConflicts(), is(pushConflicts));
    }

    private ImportSummary getImportSummary(String json) {
        ObjectMapper mapper = new ObjectMapper();
        ApiMessage apiMessage = null;
        try {
            apiMessage = mapper.readValue(json, ApiMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiMessage.getResponse().getImportSummaries().get(0);
    }
}
