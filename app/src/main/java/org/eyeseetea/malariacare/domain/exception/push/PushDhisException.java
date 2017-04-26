package org.eyeseetea.malariacare.domain.exception.push;

public class PushDhisException extends Exception {
    String message;

    public PushDhisException(String message) {
        this.message = message;
        System.out.println(PushDhisException.class.getName() + " message " + message);
    }
    public PushDhisException(Throwable throwable) {
        throwable.printStackTrace();
        this.message = throwable.getMessage();
        System.out.println(PushDhisException.class.getName() + " message " + message);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
