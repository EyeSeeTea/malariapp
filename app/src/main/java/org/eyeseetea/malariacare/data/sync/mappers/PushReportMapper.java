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

    public static Map<String, PushReport> convertImportSummaryMapToPushReportMap(
            Map<String, ImportSummary> mapEventsImportSummary) {
        Map<String, PushReport> pushReportMap = new HashMap<String, PushReport>();
        for (Map.Entry<String, ImportSummary> importSummary : mapEventsImportSummary.entrySet()) {
            pushReportMap.put(importSummary.getKey(), convertImportSummaryToPushReport(importSummary.getValue(), importSummary.getKey()));
        }
        return pushReportMap;
    }

    public static PushReport convertImportSummaryToPushReport(ImportSummary importSummary, String importSummaryKey) {
        PushReport pushReport = new PushReport();
        List<PushConflict> conflictList = new ArrayList<>();
        if (importSummary.getConflicts() != null) {
            for (Conflict conflict : importSummary.getConflicts()) {
                conflictList.add(
                        new PushConflict(conflict.getObject(), conflict.getValue()));
            }
        }
        pushReport.setPushConflicts(conflictList);
        pushReport.setDescription(importSummary.getDescription());
        pushReport.setHref(importSummary.getHref());

        ImportCount importCount = importSummary.getImportCount();
        if(importCount!=null) {
            pushReport.setPushedValuesCount(
                    new PushedValuesCount(importCount.getImported(), importCount.getUpdated(),
                            importCount.getIgnored(), importCount.getDeleted()));

        }
        pushReport.setReference(importSummary.getReference());
        if (importSummary.getStatus() == ImportSummary.Status.ERROR) {
            pushReport.setStatus(PushReport.Status.ERROR);
        }
        if (importSummary.getStatus() == ImportSummary.Status.OK) {
            pushReport.setStatus(PushReport.Status.OK);
        }
        if (importSummary.getStatus() == ImportSummary.Status.SUCCESS) {
            pushReport.setStatus(PushReport.Status.SUCCESS);
        }
        pushReport.setEventUid(importSummaryKey);
        return pushReport;
    }

}
