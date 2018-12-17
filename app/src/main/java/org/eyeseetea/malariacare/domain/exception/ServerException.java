package org.eyeseetea.malariacare.domain.exception;

public class ServerException extends Exception {
    public ServerException(int httpStatusCode, String message) {
        super("Server Error: " + httpStatusCode + " - " + message);
    }
}
