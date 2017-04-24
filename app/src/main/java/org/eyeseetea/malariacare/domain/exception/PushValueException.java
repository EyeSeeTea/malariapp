package org.eyeseetea.malariacare.domain.exception;

public class PushValueException extends Exception {
    String message;

    public PushValueException(String message) {
        this.message = message;
        System.out.println(PushValueException.class.getName() + " message " + message);
    }
    public PushValueException(Throwable throwable) {
        throwable.printStackTrace();
        this.message = throwable.getMessage();
        System.out.println(PushValueException.class.getName() + " message " + message);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
