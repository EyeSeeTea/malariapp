package org.eyeseetea.malariacare.domain.exception;


public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException() {
        super("Credentials not valid");
    }
}
