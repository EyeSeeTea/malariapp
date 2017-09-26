package org.eyeseetea.malariacare.domain.exception;


public class ClosedUserDateNotFoundException extends Exception {
    public ClosedUserDateNotFoundException(Exception e) {
        super("Closed user date not found" + e.getMessage());
        e.printStackTrace();
    }
    public ClosedUserDateNotFoundException(String message) {
        super("Closed user date not found"+ message);
        System.out.println(ConversionException.class.getName() + message);
    }
    public ClosedUserDateNotFoundException() {
        super("Closed user date not found");
    }
}
