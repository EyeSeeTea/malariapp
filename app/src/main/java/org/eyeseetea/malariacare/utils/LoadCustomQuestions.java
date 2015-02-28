package org.eyeseetea.malariacare.utils;

import org.eyeseetea.malariacare.models.ReportingResults;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose on 28/02/2015.
 */
public class LoadCustomQuestions {

    public static List<ReportingResults> addReportingQuestions()
    {
        List<ReportingResults> results=new ArrayList<ReportingResults>();

        results.add(new ReportingResults(Constants.REPORTING_Q1));
        results.add(new ReportingResults(Constants.REPORTING_Q2));
        results.add(new ReportingResults(Constants.REPORTING_Q3));
        results.add(new ReportingResults(Constants.REPORTING_Q4));
        results.add(new ReportingResults(Constants.REPORTING_Q5));
        results.add(new ReportingResults(Constants.REPORTING_Q6));
        results.add(new ReportingResults(Constants.REPORTING_Q7));
        results.add(new ReportingResults(Constants.REPORTING_Q8));
        results.add(new ReportingResults(Constants.REPORTING_Q9));
        results.add(new ReportingResults(Constants.REPORTING_Q10));

        return results;
    }

}
