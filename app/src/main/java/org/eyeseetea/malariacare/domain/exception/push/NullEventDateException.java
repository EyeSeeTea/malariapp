package org.eyeseetea.malariacare.domain.exception.push;

public class NullEventDateException extends Exception {
    String message;

    public NullEventDateException(String message) {
        this.message = message;
        System.out.println(NullEventDateException.class.getName() + " message " + message);
    }
    public NullEventDateException(Throwable throwable) {
        throwable.printStackTrace();
        this.message = throwable.getMessage();
        System.out.println(NullEventDateException.class.getName() + " message " + message);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
