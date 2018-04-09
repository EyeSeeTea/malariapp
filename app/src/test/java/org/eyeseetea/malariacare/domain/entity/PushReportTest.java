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
    public void test_import_summary_valid_push() {
        ImportSummary importSummary = getImportSummary(API_MESSAGE_WITH_CONFLICTS_JSON);
        PushReport pushReport = PushReportMapper.mapFromImportSummaryToPushReport(importSummary,
                API_MESSAGE_WITH_CONFLICTS_JSON_KEY);
        assertThat(!pushReport.hasPushErrorsWithImport(), is(true));
    }

    @Test
    public void test_error_push() {
        ImportSummary importSummary = getImportSummary(ERROR_IMPORT_SUMMARY_JSON);
        PushReport pushReport = PushReportMapper.mapFromImportSummaryToPushReport(importSummary,
                API_MESSAGE_WITH_CONFLICTS_JSON_KEY);
        assertThat(!pushReport.hasPushErrorsWithImport(), is(false));
    }
    @Test
    public void test_error_not_datavalues_imported_push() {
        ImportSummary importSummary = getImportSummary(DATAVALUES_IMPORTED_SUMMARY_JSON);
        PushReport pushReport = PushReportMapper.mapFromImportSummaryToPushReport(importSummary,
                API_MESSAGE_WITH_CONFLICTS_JSON_KEY);
        assertThat(!pushReport.hasPushErrorsWithImport(), is(false));
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
