package org.eyeseetea.malariacare.domain.exception;

public class ObservationNotFoundException extends Exception {
    public ObservationNotFoundException() {
        super("Observations not found");
    }
}
