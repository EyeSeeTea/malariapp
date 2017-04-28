package org.eyeseetea.malariacare.domain.exception;

public class ConversionException extends Exception {
    public ConversionException(Exception e) {
        super("Error in conversion");
        e.printStackTrace();
    }
    public ConversionException(String message) {
        super("Error in conversion");
        System.out.println(message);
    }
    public ConversionException() {
        super("Error in conversion");
    }
}
