package org.eyeseetea.malariacare.domain.exception;

import org.eyeseetea.malariacare.domain.entity.Observation;

public class ConversionException extends Exception {
    private Observation mObservation;

    public ConversionException(Observation observation, String message) {
        super("Error in conversion" + message);
        mObservation = observation;
    }

    //TODO: remove this constructor
    public ConversionException(Exception e) {
        super("Error in conversion" + e.getMessage());
        e.printStackTrace();
    }

    //TODO: remove this constructor
    public ConversionException(String message) {
        super("Error in conversion"+ message);
        System.out.println(ConversionException.class.getName() + message);
    }

    //TODO: remove this constructor
    public ConversionException() {
        super("Error in conversion");
    }

    public Observation getData() {
        return mObservation;
    }
}
