package org.eyeseetea.malariacare.utils;

import android.app.Activity;

import org.eyeseetea.malariacare.models.DataHolder;
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

    public static List<String> addIQAQuestions()
    {
        List<String> results = new ArrayList<String>();

       for (int i=1;i<=Constants.NUMBER_IAQ_QUESTIONS;i++)
           results.add(new Integer(i).toString());

        return results;
    }

    public static List<String> addAdherenceQuestions()
    {
        List<String> results = new ArrayList<String>();

        for (int i=1;i<=Constants.NUMBER_ADHERENCE_QUESTIONS;i++)
            results.add(new Integer(i).toString());

        return results;
    }

}
