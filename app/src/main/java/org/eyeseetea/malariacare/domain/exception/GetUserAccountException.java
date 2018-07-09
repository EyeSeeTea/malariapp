package org.eyeseetea.malariacare.domain.exception;

public class GetUserAccountException extends Exception {
    public GetUserAccountException() {
        super("Error on user attributes pull");
    }
}
