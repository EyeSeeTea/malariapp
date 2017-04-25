package org.eyeseetea.malariacare.domain.entity;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eyeseetea.malariacare.data.sync.mappers.PushReportMapper;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.hisp.dhis.client.sdk.core.common.network.ApiMessage;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;
import org.junit.Test;

import java.io.IOException;

public class PushReportTest {

    public final String SUCCESS_IMPORT_SUMMARY_JSON =
            "{\"httpStatus\":\"OK\", \"httpStatusCode\":\"200\", \"message\":\"Import was successful.\", "
                    + "\"status\":\"OK\", "
                    + "\"response\":{\"responseType\":\"ImportSummaries\", \"status\":\"SUCCESS\", "
                    + "\"importSummaries\":[{\"responseType\":\"ImportSummary\",\"status\":\"SUCCESS\","
                    + "\"description\": \"\",  \"importCount\""
                    + ":{ \"imported\": \"4 \",  \"updated\": \"0 \",  \"ignored\": \"0 \",  \"deleted\": \"0 \"}, "
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

    public final String COMPLETE_IMPORT_SUMMARY_KEY = "DSifqmkzKfJ";
    public final String API_MESSAGE_WITH_CONFLICTS_JSON_KEY = "wqee94y5wzT";

    @Test
    public void test_import_summary_conversion_on_success_json() {
        ImportSummary importSummary = getImportSummary(SUCCESS_IMPORT_SUMMARY_JSON);
        PushReport pushReport = PushReportMapper.convertImportSummaryToPushReport(importSummary,
                COMPLETE_IMPORT_SUMMARY_KEY);

        assertThat(pushReport.getDescription().equals(importSummary.getDescription()), is(true));
        assertThat(pushReport.getEventUid().equals(importSummary.getReference()), is(true));
        assertThat(pushReport.getHref().equals(importSummary.getHref()), is(true));
        assertThat((pushReport.getStatus().equals(PushReport.Status.SUCCESS)), is(true));
        assertThat((importSummary.getStatus().equals(ImportSummary.Status.SUCCESS)), is(true));
        assertThat(pushReport.getPushedValues().getImported() == (importSummary.getImportCount()
                .getImported()), is(true));
        assertThat(pushReport.getPushedValues().getDeleted() == (importSummary.getImportCount()
                .getDeleted()), is(true));
        assertThat(pushReport.getPushedValues().getIgnored() == (importSummary.getImportCount()
                .getIgnored()), is(true));
        assertThat(pushReport.getPushedValues().getUpdated() == (importSummary.getImportCount()
                .getUpdated()), is(true));
        assertThat(pushReport.getPushConflicts().isEmpty(), is(true));
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

    @Test
    public void test_import_summary_conversion_on_success_with_conflicts_from_Api_message() {
        ImportSummary importSummary = getImportSummary(API_MESSAGE_WITH_CONFLICTS_JSON);
        PushReport pushReport = PushReportMapper.convertImportSummaryToPushReport(importSummary,
                API_MESSAGE_WITH_CONFLICTS_JSON_KEY);

        assertThat(pushReport.getEventUid().equals(importSummary.getReference()), is(true));
        assertThat(pushReport.getHref().equals(importSummary.getHref()), is(true));
        assertThat((pushReport.getStatus().equals(PushReport.Status.SUCCESS)), is(true));
        assertThat((importSummary.getStatus().equals(ImportSummary.Status.SUCCESS)), is(true));
        assertThat(pushReport.getPushedValues().getImported() == (importSummary.getImportCount()
                .getImported()), is(true));
        assertThat(pushReport.getPushedValues().getDeleted() == (importSummary.getImportCount()
                .getDeleted()), is(true));
        assertThat(pushReport.getPushedValues().getIgnored() == (importSummary.getImportCount()
                .getIgnored()), is(true));
        assertThat(pushReport.getPushedValues().getUpdated() == (importSummary.getImportCount()
                .getUpdated()), is(true));

        assertThat(pushReport.getPushConflicts().get(0).getUid().equals(
                importSummary.getConflicts().get(0).getObject()), is(true));
        assertThat(pushReport.getPushConflicts().get(0).getValue().equals(
                importSummary.getConflicts().get(0).getValue()), is(true));

    }
}
