package org.eyeseetea.malariacare.domain.exception;


public class NetworkException extends Exception {
    public NetworkException() {
        super("Network not available");
    }
}
