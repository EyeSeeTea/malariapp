package org.eyeseetea.malariacare.domain.exception;


public class NoLoginException extends Exception {
    public NoLoginException() {
        super("No exists logged user, please first realize login");
    }
}
