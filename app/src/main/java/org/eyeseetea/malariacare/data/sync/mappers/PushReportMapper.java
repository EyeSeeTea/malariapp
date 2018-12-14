package org.eyeseetea.malariacare.data.sync.mappers;


import org.eyeseetea.malariacare.domain.entity.pushsummary.PushConflict;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushedValuesCount;
import org.hisp.dhis.client.sdk.models.common.importsummary.Conflict;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportCount;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PushReportMapper {

    public static Map<String, PushReport> mapFromImportSummariesToPushReports(
            Map<java.lang.String, ImportSummary> mapEventsImportSummary) {
        Map<String, PushReport> pushReportMap = new HashMap<String, PushReport>();
        for (Map.Entry<String, ImportSummary> importSummary : mapEventsImportSummary.entrySet()) {
            pushReportMap.put(importSummary.getKey(),
                    mapFromImportSummaryToPushReport(importSummary.getValue(),
                            importSummary.getKey()));
        }
        return pushReportMap;
    }

    public static PushReport mapFromImportSummaryToPushReport(ImportSummary importSummary,
            String importSummaryKey) {
        List<PushConflict> conflictList = new ArrayList<>();
        if (importSummary.getConflicts() != null) {
            for (Conflict conflict : importSummary.getConflicts()) {
                conflictList.add(
                        new PushConflict(conflict.getObject(), conflict.getValue()));
            }
        }
        String description = importSummary.getDescription();
        String href = importSummary.getHref();

        ImportCount importCount = importSummary.getImportCount();
        PushedValuesCount pushedValuesCount = null;
        if(importCount!=null) {
            pushedValuesCount = new PushedValuesCount(importCount.getImported(),
                    importCount.getUpdated(), importCount.getIgnored(), importCount.getDeleted());

        }
        String reference = importSummary.getReference();
        PushReport.Status status = null;
        if (importSummary.getStatus() == ImportSummary.Status.ERROR) {
            status = PushReport.Status.ERROR;
        }
        if (importSummary.getStatus() == ImportSummary.Status.OK) {
            status = PushReport.Status.OK;
        }
        if (importSummary.getStatus() == ImportSummary.Status.SUCCESS) {
            status = PushReport.Status.SUCCESS;
        }

        return new PushReport(importSummaryKey, status, description, pushedValuesCount, reference,
                href, conflictList);
    }
}
