package org.eyeseetea.malariacare.domain.exception;

public class ConversionException extends Exception {
    public ConversionException(Exception e) {
        super("Error in conversion");
        e.printStackTrace();
    }
    public ConversionException() {
        super("Error in conversion");
    }
}
