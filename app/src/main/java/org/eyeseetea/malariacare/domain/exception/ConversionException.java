package org.eyeseetea.malariacare.domain.exception;

public class ConversionException extends Exception {
    public ConversionException(Exception e) {
        super("Error in conversion" + e.getMessage());
        e.printStackTrace();
    }
    public ConversionException(String message) {
        super("Error in conversion"+ message);
        System.out.println(ConversionException.class.getName() + message);
    }
    public ConversionException() {
        super("Error in conversion");
    }
}
