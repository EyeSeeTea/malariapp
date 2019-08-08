package org.eyeseetea.malariacare.domain.exception;

import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;


public class ConversionException extends Exception {
    private SurveyDB conversionFailedSurvey;
    private ObsActionPlanDB conversionFailedObsPan;

    public ConversionException() {
        super("Error in conversion");
    }

    public ConversionException(String message) {
        super("Error in survey conversion: " + message);
    }

    public ConversionException(Exception e) {
        super("Error in conversion" + e.getMessage());
        e.printStackTrace();
    }

    public ConversionException(SurveyDB conversionFailedSurvey, String message) {
        super("Error in survey conversion: " + message);
        this.conversionFailedSurvey = conversionFailedSurvey;
    }

    public ConversionException(ObsActionPlanDB conversionFailedObsPan, String message) {
        super("Error in obs & action plan conversion: " + message);
        this.conversionFailedObsPan = conversionFailedObsPan;
    }

    public SurveyDB getConversionFailedSurvey() {
        return conversionFailedSurvey;
    }

    public ObsActionPlanDB getConversionFailedObsPan() {
        return conversionFailedObsPan;
    }
}
